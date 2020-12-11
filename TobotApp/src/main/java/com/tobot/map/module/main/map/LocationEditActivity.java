package com.tobot.map.module.main.map;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.slamtec.slamware.robot.Pose;
import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;
import com.tobot.slam.data.LocationBean;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2020/3/14
 */
public class LocationEditActivity extends BaseActivity implements SensorAreaDialog.OnSensorAreaListener {
    @BindView(R.id.tv_head)
    TextView tvHead;
    @BindView(R.id.et_number)
    EditText etNumber;
    @BindView(R.id.et_name_china)
    EditText etNameChina;
    @BindView(R.id.et_name_english)
    EditText etNameEnglish;
    @BindView(R.id.tv_config_ultrasonic)
    TextView tvUltrasonicSwitch;
    private String mNumber;
    private boolean isRequestPose;
    private LocationThread mLocationThread;
    private Pose mPose;
    private int mUltrasonicStatus;
    private SensorAreaDialog mSensorAreaDialog;
    private LocationBean mLocationBean;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_location_edit;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_title_edit_location);
        // 超声波默认打开状态
        mUltrasonicStatus = SlamCode.ULTRASONIC_STATUS_OPEN;
        LocationBean bean = getIntent().getParcelableExtra(BaseConstant.DATA_KEY);
        mLocationBean = bean;
        if (bean != null) {
            mNumber = bean.getLocationNumber();
            etNumber.setText(mNumber);
            etNameChina.setText(bean.getLocationNameChina());
            etNameEnglish.setText(bean.getLocationNameEnglish());
            mUltrasonicStatus = bean.getSensorStatus();
        }
        tvUltrasonicSwitch.setSelected(mUltrasonicStatus == SlamCode.ULTRASONIC_STATUS_OPEN);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationThread != null) {
            mLocationThread.interrupt();
            mLocationThread = null;
        }
        if (isSensorAreaDialogShow()) {
            mSensorAreaDialog.getDialog().dismiss();
            mSensorAreaDialog = null;
        }
    }

    @Override
    public void onSensorArea(float startX, float startY, float endX, float endY) {
        if (mLocationBean != null) {
            mLocationBean.setStartX(startX);
            mLocationBean.setStartY(startY);
            mLocationBean.setEndX(endX);
            mLocationBean.setEndY(endY);
        }
    }

    @OnClick({R.id.btn_update_location, R.id.tv_config_ultrasonic, R.id.btn_ultrasonic_area, R.id.btn_confirm})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.btn_update_location:
                isRequestPose = true;
                mLocationThread = new LocationThread();
                mLocationThread.start();
                break;
            case R.id.tv_config_ultrasonic:
                if (tvUltrasonicSwitch.isSelected()) {
                    mUltrasonicStatus = SlamCode.ULTRASONIC_STATUS_CLOSE;
                } else {
                    mUltrasonicStatus = SlamCode.ULTRASONIC_STATUS_OPEN;
                }
                tvUltrasonicSwitch.setSelected(mUltrasonicStatus == SlamCode.ULTRASONIC_STATUS_OPEN);
                break;
            case R.id.btn_ultrasonic_area:
                if (!isSensorAreaDialogShow()) {
                    mSensorAreaDialog = SensorAreaDialog.newInstance(mLocationBean);
                    mSensorAreaDialog.setOnSensorAreaListener(this);
                    mSensorAreaDialog.show(getFragmentManager(), "SENSOR_AREA_DIALOG");
                }
                break;
            case R.id.btn_confirm:
                confirm();
                break;
            default:
                break;
        }
    }

    private void confirm() {
        String number = etNumber.getText().toString().trim();
        String nameChina = etNameChina.getText().toString().trim();
        String nameEnglish = etNameEnglish.getText().toString().trim();
        if (TextUtils.isEmpty(number)) {
            showToastTips(getString(R.string.name_empty_tips));
            return;
        }
        // 如果修改编号，则不能与存在的编号一样
        if (!TextUtils.equals(number, mNumber)) {
            LocationBean bean = MyDBSource.getInstance(this).queryLocation(number);
            // 如果已经包含该名称的话，就不让更改
            if (bean != null) {
                showToastTips(getString(R.string.number_edit_fail_tips));
                return;
            }
        }
        // 中文名称不为空的情况
        if (!TextUtils.isEmpty(nameChina)) {
            LocationBean bean = MyDBSource.getInstance(this).queryLocationByChineseName(nameChina);
            if (bean != null) {
                // 如果该名称是在要修改的编号中，则不处理
                if (!TextUtils.equals(bean.getLocationNumber(), mNumber)) {
                    showToastTips(getString(R.string.name_china_edit_fail_tips));
                    return;
                }
            }
        }
        // 英文名称不为空的情况
        if (!TextUtils.isEmpty(nameEnglish)) {
            LocationBean bean = MyDBSource.getInstance(this).queryLocationByEnglishName(nameEnglish);
            if (bean != null) {
                // 如果该名称是在要修改的编号中，则不处理
                if (!TextUtils.equals(bean.getLocationNumber(), mNumber)) {
                    showToastTips(getString(R.string.name_english_edit_fail_tips));
                    return;
                }
            }
        }
        // 更新点位置的情况
        if (isRequestPose && mPose == null) {
            isRequestPose = false;
            showToastTips(getString(R.string.pose_request_fail_tips));
            return;
        }

        if (mLocationBean != null) {
            mLocationBean.setLocationNumber(number);
            mLocationBean.setLocationNameChina(nameChina);
            mLocationBean.setLocationNameEnglish(nameEnglish);
            mLocationBean.setSensorStatus(mUltrasonicStatus);
            if (mPose != null) {
                mLocationBean.setX(mPose.getX());
                mLocationBean.setY(mPose.getY());
                mLocationBean.setYaw(mPose.getYaw());
            }
            MyDBSource.getInstance(this).updateLocation(mNumber, mLocationBean);
        }

        Intent data = new Intent();
        data.putExtra(BaseConstant.NUMBER_KEY, mNumber);
        data.putExtra(BaseConstant.DATA_KEY, mLocationBean);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private boolean isSensorAreaDialogShow() {
        return mSensorAreaDialog != null && mSensorAreaDialog.getDialog() != null && mSensorAreaDialog.getDialog().isShowing();
    }

    private class LocationThread extends Thread {

        @Override
        public void run() {
            super.run();
            mPose = SlamManager.getInstance().getPose();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mPose != null) {
                        showToastTips(getString(R.string.pose_request_success_tips));
                    }
                }
            });
        }
    }
}
