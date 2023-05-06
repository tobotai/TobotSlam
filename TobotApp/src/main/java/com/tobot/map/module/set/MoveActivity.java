package com.tobot.map.module.set;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.util.NumberUtils;
import com.tobot.map.util.SystemUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnResultListener;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2022/06/30
 */
public class MoveActivity extends BaseBackActivity implements OnResultListener<Boolean> {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_rotate_value)
    EditText etRotate;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_navigate_distance)
    EditText etNavigate;
    private boolean isRotateLeft = true;
    private boolean isNavigate;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_move;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_move);
    }

    @Override
    public void onResult(Boolean data) {
        showToastTips(getString(isNavigate ? R.string.navigate_result : R.string.rotate_result, data));
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.rb_to_left, R.id.rb_to_right, R.id.btn_send_rotate_value, R.id.btn_send_navigate_distance})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.rb_to_left:
                isRotateLeft = true;
                break;
            case R.id.rb_to_right:
                isRotateLeft = false;
                break;
            case R.id.btn_send_rotate_value:
                sendRotate();
                break;
            case R.id.btn_send_navigate_distance:
                sendNavigate();
                break;
            default:
                break;
        }
    }

    private void sendRotate() {
        SystemUtils.hideKeyboard(this);
        String content = etRotate.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showToastTips(getString(R.string.rotate_speed_empty_tips));
            return;
        }

        if (!SlamManager.getInstance().isConnected()) {
            showToastTips(getString(R.string.slam_not_connect_tips));
            return;
        }

        if (TextUtils.isDigitsOnly(content)) {
            int value = Integer.parseInt(content);
            Logger.i(BaseConstant.TAG, "rotate() value=" + value);
            isNavigate = false;
            SlamManager.getInstance().rotate(value, isRotateLeft, this);
        }
    }

    private void sendNavigate() {
        SystemUtils.hideKeyboard(this);
        String content = etNavigate.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            showToastTips(getString(R.string.navigate_distance_empty_tips));
            return;
        }

        if (!SlamManager.getInstance().isConnected()) {
            showToastTips(getString(R.string.slam_not_connect_tips));
            return;
        }

        if (NumberUtils.isDoubleOrFloat(content)) {
            float distance = Float.parseFloat(content);
            Logger.i(BaseConstant.TAG, "navigate distance=" + distance);
            isNavigate = true;
            SlamManager.getInstance().moveToDistance(distance, this);
        }
    }
}
