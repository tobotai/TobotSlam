package com.tobot.map.module.set;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.NumberUtils;
import com.tobot.map.util.SystemUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2021/08/25
 */
public class RunParameterActivity extends BaseActivity {
    @BindView(R.id.tv_head)
    TextView tvHead;
    @BindView(R.id.tv_chassis_radius)
    TextView tvChassisRadius;
    @BindView(R.id.et_chassis_radius)
    EditText etChassisRadius;
    @BindView(R.id.rb_relocation_global)
    RadioButton rbRelocationGlobal;
    @BindView(R.id.rb_relocation_part)
    RadioButton rbRelocationPart;
    @BindView(R.id.tv_relocation_quality_min)
    TextView tvRelocationQualityMin;
    @BindView(R.id.et_relocation_quality_min)
    EditText etRelocationQualityMin;
    @BindView(R.id.tv_relocation_quality_safe)
    TextView tvRelocationQualitySafe;
    @BindView(R.id.et_relocation_quality_safe)
    EditText etRelocationQualitySafe;
    @BindView(R.id.tv_charge_start_distance)
    TextView tvChargeDistance;
    @BindView(R.id.et_charge_start_distance)
    EditText etChargeDistance;
    @BindView(R.id.tv_charge_offset)
    TextView tvChargeOffset;
    @BindView(R.id.et_charge_offset)
    EditText etChargeOffset;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_run_parameter;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_navigate_parameter);
        setChassisRadiusTips(DataHelper.getInstance().getChassisRadius(this));
        setRelocationQualityMinTips(SlamManager.getInstance().getRelocationQualityMin());
        setRelocationQualitySafeTips(SlamManager.getInstance().getRelocationQualitySafe());
        setChargeStartDistanceTips(DataHelper.getInstance().getChargeDistance(this));
        setChargeOffsetTips(DataHelper.getInstance().getChargeOffset(this));
        if (DataHelper.getInstance().getRelocationType(this) == SlamCode.RELOCATION_GLOBAL) {
            rbRelocationGlobal.setChecked(true);
            return;
        }

        rbRelocationPart.setChecked(true);
    }

    @OnClick({R.id.btn_set_chassis_radius, R.id.rb_relocation_global, R.id.rb_relocation_part, R.id.btn_set_relocation_quality_min, R.id.btn_set_relocation_quality_safe,
            R.id.btn_charge_start_distance, R.id.btn_charge_offset})
    public void onClickView(View v) {
        switch (v.getId()) {
            case R.id.btn_set_chassis_radius:
                setChassisRadius();
                break;
            case R.id.rb_relocation_global:
                DataHelper.getInstance().setRelocationType(this, SlamCode.RELOCATION_GLOBAL);
                break;
            case R.id.rb_relocation_part:
                DataHelper.getInstance().setRelocationType(this, SlamCode.RELOCATION_PART);
                break;
            case R.id.btn_set_relocation_quality_min:
                setRelocationQualityMin();
                break;
            case R.id.btn_set_relocation_quality_safe:
                setRelocationQualitySafe();
                break;
            case R.id.btn_charge_start_distance:
                setChargeStartDistance();
                break;
            case R.id.btn_charge_offset:
                setChargeOffset();
                break;
            default:
                break;
        }
    }

    private void setChassisRadiusTips(float value) {
        Logger.i(BaseConstant.TAG, "chassisRadius=" + value);
        tvChassisRadius.setText(getString(R.string.tv_chassis_radius, String.valueOf(value)));
    }

    private void setRelocationQualityMinTips(int value) {
        tvRelocationQualityMin.setText(getString(R.string.tv_relocation_quality_min, value));
    }

    private void setRelocationQualitySafeTips(int value) {
        tvRelocationQualitySafe.setText(getString(R.string.tv_relocation_quality_safe, value));
    }

    private void setChargeStartDistanceTips(float value) {
        tvChargeDistance.setText(getString(R.string.tv_charge_start_distance, String.valueOf(value)));
    }

    private void setChargeOffsetTips(float value) {
        tvChargeOffset.setText(getString(R.string.tv_charge_offset, String.valueOf(value)));
    }

    private void setChassisRadius() {
        String content = etChassisRadius.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showToastTips(getString(R.string.chassis_radius_empty_tips));
            return;
        }

        SystemUtils.hideKeyboard(this);
        if (NumberUtils.isDoubleOrFloat(content)) {
            float value = Float.parseFloat(content);
            DataHelper.getInstance().setChassisRadius(this, value);
            setChassisRadiusTips(value);
            showToastTips(getString(R.string.set_success_tips));
            etChassisRadius.setText("");
        }
    }

    private void setRelocationQualityMin() {
        String content = etRelocationQualityMin.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showToastTips(getString(R.string.relocation_quality_empty_tips));
            return;
        }

        SystemUtils.hideKeyboard(this);
        if (TextUtils.isDigitsOnly(content)) {
            int value = Integer.parseInt(content);
            DataHelper.getInstance().setRelocationQualityMin(this, value);
            SlamManager.getInstance().setRelocationQualityMin(value);
            setRelocationQualityMinTips(value);
            showToastTips(getString(R.string.set_success_tips));
            etRelocationQualityMin.setText("");
        }
    }

    private void setRelocationQualitySafe() {
        String content = etRelocationQualitySafe.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showToastTips(getString(R.string.relocation_quality_empty_tips));
            return;
        }

        SystemUtils.hideKeyboard(this);
        if (TextUtils.isDigitsOnly(content)) {
            int value = Integer.parseInt(content);
            DataHelper.getInstance().setRelocationQualitySafe(this, value);
            SlamManager.getInstance().setRelocationQualitySafe(value);
            setRelocationQualitySafeTips(value);
            showToastTips(getString(R.string.set_success_tips));
            etRelocationQualitySafe.setText("");
        }
    }

    private void setChargeStartDistance() {
        String content = etChargeDistance.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showToastTips(getString(R.string.charge_distance_empty_tips));
            return;
        }

        SystemUtils.hideKeyboard(this);
        if (NumberUtils.isDoubleOrFloat(content)) {
            float value = Float.parseFloat(content);
            DataHelper.getInstance().setChargeDistance(this, value);
            setChargeStartDistanceTips(value);
            showToastTips(getString(R.string.set_success_tips));
            etChargeDistance.setText("");
        }
    }

    private void setChargeOffset() {
        String content = etChargeOffset.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showToastTips(getString(R.string.charge_offset_empty_tips));
            return;
        }

        SystemUtils.hideKeyboard(this);
        if (NumberUtils.isDoubleOrFloat(content)) {
            float value = Float.parseFloat(content);
            DataHelper.getInstance().setChargeOffset(this, value);
            setChargeOffsetTips(value);
            showToastTips(getString(R.string.set_success_tips));
            etChargeOffset.setText("");
        }
    }
}
