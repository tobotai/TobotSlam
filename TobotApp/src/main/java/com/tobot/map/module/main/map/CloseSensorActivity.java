package com.tobot.map.module.main.map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.util.NumberUtils;
import com.tobot.map.util.SystemUtils;
import com.tobot.slam.agent.SlamCode;
import com.tobot.slam.data.LocationBean;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2021/08/31
 */
public class CloseSensorActivity extends BaseBackActivity implements View.OnClickListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rg_sonar_status)
    RadioGroup rgSonarStatus;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_sonar)
    RadioButton rbSonar;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_cliff)
    RadioButton rbCliff;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rb_all)
    RadioButton rbAll;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_show_area_edit)
    LinearLayout llShowArea;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_area_width)
    EditText etWidth;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_area_height)
    EditText etHeight;
    private LocationBean mLocationBean;
    private int mSensorStatus = SlamCode.STATUS_SENSOR_OPEN;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_close_sensor;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.btn_close_sensor);
        mLocationBean = getIntent().getParcelableExtra(BaseConstant.DATA_KEY);
        setStatus(mLocationBean);
        for (int i = 0, size = rgSonarStatus.getChildCount(); i < size; i++) {
            rgSonarStatus.getChildAt(i).setOnClickListener(this);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.rb_sonar:
                handleRadioButtonClick(id, SlamCode.STATUS_SENSOR_SONAR_CLOSE);
                break;
            case R.id.rb_cliff:
                handleRadioButtonClick(id, SlamCode.STATUS_SENSOR_CLIFF_CLOSE);
                break;
            case R.id.rb_all:
                handleRadioButtonClick(id, SlamCode.STATUS_SENSOR_ALL_CLOSE);
                break;
            default:
                break;
        }
    }

    @OnClick({R.id.btn_confirm})
    public void onClickView(View view) {
        if (view.getId() == R.id.btn_confirm) {
            confirm();
        }
    }

    private void setStatus(LocationBean bean) {
        if (bean == null) {
            return;
        }

        int status = bean.getSensorStatus();
        switch (status) {
            case SlamCode.STATUS_SENSOR_ALL_CLOSE:
                rbAll.setChecked(true);
                break;
            case SlamCode.STATUS_SENSOR_SONAR_CLOSE:
                rbSonar.setChecked(true);
                break;
            case SlamCode.STATUS_SENSOR_CLIFF_CLOSE:
                rbCliff.setChecked(true);
                break;
            default:
                status = SlamCode.STATUS_SENSOR_OPEN;
                break;
        }

        mSensorStatus = status;
        boolean isSensorOpen = status == SlamCode.STATUS_SENSOR_OPEN;
        llShowArea.setVisibility(isSensorOpen ? View.GONE : View.VISIBLE);
        if (!isSensorOpen) {
            etWidth.setText(String.valueOf(Math.abs(bean.getEndX() - bean.getStartX())));
            etHeight.setText(String.valueOf(Math.abs(bean.getEndY() - bean.getStartY())));
        }
    }

    private void handleRadioButtonClick(int id, int status) {
        if (mSensorStatus == status) {
            rgSonarStatus.clearCheck();
            mSensorStatus = SlamCode.STATUS_SENSOR_OPEN;
            llShowArea.setVisibility(View.GONE);
            return;
        }

        mSensorStatus = status;
        rgSonarStatus.check(id);
        llShowArea.setVisibility(View.VISIBLE);
    }

    private void confirm() {
        SystemUtils.hideKeyboard(this);
        float widthHalf = 0;
        float heightHalf = 0;
        if (mSensorStatus != SlamCode.STATUS_SENSOR_OPEN) {
            String widthStr = etWidth.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();
            if (TextUtils.isEmpty(widthStr)) {
                showToastTips(getString(R.string.area_width_empty_tips));
                return;
            }

            if (TextUtils.isEmpty(heightStr)) {
                showToastTips(getString(R.string.area_height_empty_tips));
                return;
            }

            widthHalf = NumberUtils.isDoubleOrFloat(widthStr) ? Float.parseFloat(widthStr) / 2.0f : 0;
            heightHalf = NumberUtils.isDoubleOrFloat(heightStr) ? Float.parseFloat(heightStr) / 2.0f : 0;
        }

        boolean isSet = widthHalf > 0 && heightHalf > 0;
        if (mLocationBean != null) {
            mLocationBean.setSensorStatus(mSensorStatus);
            // 如果没设置的话，则默认为0
            mLocationBean.setStartX(isSet ? mLocationBean.getX() - widthHalf : 0);
            mLocationBean.setStartY(isSet ? mLocationBean.getY() + heightHalf : 0);
            mLocationBean.setEndX(isSet ? mLocationBean.getX() + widthHalf : 0);
            mLocationBean.setEndY(isSet ? mLocationBean.getY() - heightHalf : 0);
        }

        showToastTips(getString(R.string.set_success_tips));
        Intent data = new Intent();
        data.putExtra(BaseConstant.DATA_KEY, mLocationBean);
        setResult(Activity.RESULT_OK, data);
        finish();
    }
}
