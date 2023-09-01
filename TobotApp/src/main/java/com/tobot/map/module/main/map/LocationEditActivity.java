package com.tobot.map.module.main.map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.slamtec.slamware.robot.Pose;
import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.log.Logger;
import com.tobot.map.util.NumberUtils;
import com.tobot.map.util.SystemUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;
import com.tobot.slam.data.LocationBean;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2020/3/14
 */
public class LocationEditActivity extends BaseBackActivity implements View.OnClickListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_number)
    EditText etNumber;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_name_china)
    EditText etNameChina;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_name_english)
    EditText etNameEnglish;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_mark_location)
    RadioButton rbMarkLocation;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_relocation)
    RadioButton rbRelocation;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_arrive_not_rotate)
    RadioButton rbArriveNotRotate;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rg_location_type)
    RadioGroup rgLocationType;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_show_relocation_area)
    LinearLayout llShowRelocationArea;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_relocation_area_width)
    EditText etRelocationWidth;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_relocation_area_height)
    EditText etRelocationHeight;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_close_sensor)
    Button btnCloseSensor;
    private String mNumber;
    private LocationBean mLocationBean;
    private int mLocationType = SlamCode.TYPE_IDLE;
    private float mWidthHalf, mHeightHalf;
    private boolean isCloseSensor;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_location_edit;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_title_edit_location);
        // 超声波默认打开状态
        LocationBean bean = getIntent().getParcelableExtra(BaseConstant.DATA_KEY);
        mLocationBean = bean;
        if (bean != null) {
            mNumber = bean.getLocationNumber();
            etNumber.setText(mNumber);
            etNameChina.setText(bean.getLocationNameChina());
            etNameEnglish.setText(bean.getLocationNameEnglish());
            setLocationType(bean);
        }

        for (int i = 0, size = rgLocationType.getChildCount(); i < size; i++) {
            rgLocationType.getChildAt(i).setOnClickListener(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            LocationBean bean = data.getParcelableExtra(BaseConstant.DATA_KEY);
            if (bean != null) {
                isCloseSensor = true;
                // 如果设置了传感器区域，就不能再设置定位区域
                mWidthHalf = 0;
                mHeightHalf = 0;
                etRelocationWidth.setText("");
                etRelocationHeight.setText("");
                if (mLocationBean != null) {
                    mLocationBean.setSensorStatus(bean.getSensorStatus());
                    // 如果没设置的话，则默认为0
                    mLocationBean.setStartX(bean.getStartX());
                    mLocationBean.setStartY(bean.getStartY());
                    mLocationBean.setEndX(bean.getEndX());
                    mLocationBean.setEndY(bean.getEndY());
                }
            }
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rb_mark_location:
                handleRadioButtonClick(id, SlamCode.TYPE_MARK_LOCATION);
                break;
            case R.id.rb_relocation:
                handleRadioButtonClick(id, SlamCode.TYPE_RELOCATION);
                break;
            case R.id.rb_arrive_not_rotate:
                handleRadioButtonClick(id, SlamCode.TYPE_ARRIVE_NOT_ROTATE);
                break;
            default:
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btn_update_location, R.id.btn_set, R.id.btn_close_sensor, R.id.btn_confirm})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.btn_update_location:
                ThreadPoolManager.getInstance().execute(new LocationRunnable());
                break;
            case R.id.btn_set:
                setRelocationArea();
                break;
            case R.id.btn_close_sensor:
                Intent intent = new Intent(this, CloseSensorActivity.class);
                intent.putExtra(BaseConstant.DATA_KEY, mLocationBean);
                startActivityForResult(intent, 1);
                break;
            case R.id.btn_confirm:
                confirm();
                break;
            default:
                break;
        }
    }

    private void setLocationType(LocationBean bean) {
        mLocationType = bean.getType();
        switch (mLocationType) {
            case SlamCode.TYPE_MARK_LOCATION:
                rbMarkLocation.setChecked(true);
                llShowRelocationArea.setVisibility(View.GONE);
                btnCloseSensor.setVisibility(View.GONE);
                break;
            case SlamCode.TYPE_RELOCATION:
                rbRelocation.setChecked(true);
                btnCloseSensor.setVisibility(View.GONE);
                llShowRelocationArea.setVisibility(View.VISIBLE);
                etRelocationWidth.setText(String.valueOf(Math.abs(bean.getEndX() - bean.getStartX())));
                etRelocationHeight.setText(String.valueOf(Math.abs(bean.getEndY() - bean.getStartY())));
                break;
            case SlamCode.TYPE_ARRIVE_NOT_ROTATE:
                rbArriveNotRotate.setChecked(true);
                llShowRelocationArea.setVisibility(View.GONE);
                btnCloseSensor.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void handleRadioButtonClick(int id, int type) {
        if (mLocationType == type) {
            rgLocationType.clearCheck();
            mLocationType = SlamCode.TYPE_IDLE;
            if (type == SlamCode.TYPE_RELOCATION) {
                llShowRelocationArea.setVisibility(View.GONE);
            }
            btnCloseSensor.setVisibility(View.VISIBLE);
            return;
        }

        mLocationType = type;
        rgLocationType.check(id);
        llShowRelocationArea.setVisibility(type == SlamCode.TYPE_RELOCATION ? View.VISIBLE : View.GONE);
        btnCloseSensor.setVisibility(View.GONE);
        // 先关闭传感器后又点击的情况
        if (isCloseSensor) {
            isCloseSensor = false;
            if (mLocationBean != null) {
                mLocationBean.setSensorStatus(SlamCode.STATUS_SENSOR_OPEN);
                mLocationBean.setStartX(0);
                mLocationBean.setStartY(0);
                mLocationBean.setEndX(0);
                mLocationBean.setEndY(0);
            }
        }
    }

    private void setRelocationArea() {
        SystemUtils.hideKeyboard(this);
        String widthStr = etRelocationWidth.getText().toString().trim();
        String heightStr = etRelocationHeight.getText().toString().trim();
        if (mLocationBean == null) {
            return;
        }

        mWidthHalf = 0;
        if (NumberUtils.isDoubleOrFloat(widthStr)) {
            mWidthHalf = Float.parseFloat(widthStr) / 2.0f;
        }

        mHeightHalf = 0;
        if (NumberUtils.isDoubleOrFloat(heightStr)) {
            mHeightHalf = Float.parseFloat(heightStr) / 2.0f;
        }
        showToastTips(getString(R.string.set_success));
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

        SystemUtils.hideKeyboard(this);
        if (mLocationBean != null) {
            mLocationBean.setLocationNumber(number);
            mLocationBean.setLocationNameChina(nameChina);
            mLocationBean.setLocationNameEnglish(nameEnglish);
            mLocationBean.setType(mLocationType);
            // 设置重定位区域
            if (mLocationType == SlamCode.TYPE_RELOCATION) {
                Logger.i(BaseConstant.TAG, "widthHalf=" + mWidthHalf + ",heightHalf=" + mHeightHalf);
                if (mWidthHalf > 0 && mHeightHalf > 0) {
                    mLocationBean.setStartX(mLocationBean.getX() - mWidthHalf);
                    mLocationBean.setStartY(mLocationBean.getY() + mHeightHalf);
                    mLocationBean.setEndX(mLocationBean.getX() + mWidthHalf);
                    mLocationBean.setEndY(mLocationBean.getY() - mHeightHalf);
                }
            }

            MyDBSource.getInstance(this).updateLocation(mNumber, mLocationBean);
        }

        Intent data = new Intent();
        data.putExtra(BaseConstant.NUMBER_KEY, mNumber);
        data.putExtra(BaseConstant.DATA_KEY, mLocationBean);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    private class LocationRunnable implements Runnable {
        @Override
        public void run() {
            Pose pose = SlamManager.getInstance().getPose();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean isSuccess = false;
                    if (pose != null) {
                        isSuccess = true;
                        if (mLocationBean != null) {
                            mLocationBean.setX(pose.getX());
                            mLocationBean.setY(pose.getY());
                            mLocationBean.setYaw(pose.getYaw());
                        }
                    }

                    showToastTips(getString(isSuccess ? R.string.pose_request_success_tips : R.string.pose_request_fail_tips));
                }
            });
        }
    }
}
