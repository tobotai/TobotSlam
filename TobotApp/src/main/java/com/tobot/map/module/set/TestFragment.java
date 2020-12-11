package com.tobot.map.module.set;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.slamtec.slamware.action.MoveDirection;
import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.slam.SlamManager;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2020/5/6
 */
public class TestFragment extends BaseFragment {
    @BindView(R.id.et_speed)
    EditText etSpeed;
    private MoveDirection mRotateDirection = MoveDirection.TURN_LEFT;
    private int mSpeedValue;
    private TestThread mTestThread;

    public static TestFragment newInstance() {
        return new TestFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_test;
    }

    @Override
    protected void init() {
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mTestThread != null) {
            mTestThread.interrupt();
            mTestThread = null;
        }
    }

    @OnClick({R.id.rb_to_left, R.id.rb_to_right, R.id.btn_send})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.rb_to_left:
                mRotateDirection = MoveDirection.TURN_LEFT;
                break;
            case R.id.rb_to_right:
                mRotateDirection = MoveDirection.TURN_RIGHT;
                break;
            case R.id.btn_send:
                send();
                break;
            default:
                break;
        }
    }

    private void send() {
        String speed = etSpeed.getText().toString().trim();
        if (TextUtils.isEmpty(speed)) {
            showToastTips(getString(R.string.rotate_speed_empty_tips));
            return;
        }

        if (TextUtils.isDigitsOnly(speed)) {
            mSpeedValue = Integer.parseInt(speed);
            mTestThread = new TestThread(new WeakReference<>(this));
            mTestThread.start();
        }
    }

    private void rotate() {
        SlamManager.getInstance().rotate(mSpeedValue, mRotateDirection);
    }

    private static class TestThread extends Thread {
        private TestFragment mFragment;

        private TestThread(WeakReference<TestFragment> reference) {
            mFragment = reference.get();
        }

        @Override
        public void run() {
            super.run();
            if (mFragment != null) {
                mFragment.rotate();
            }
        }
    }
}
