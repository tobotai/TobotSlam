package com.tobot.map.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

import com.tobot.map.module.common.ConfirmDialog;
import com.tobot.map.module.common.LoadTipsDialog;
import com.tobot.map.util.ToastUtils;

import butterknife.ButterKnife;

/**
 * @author houdeming
 * @date 2018/7/20
 */
public abstract class BaseActivity extends FragmentActivity implements ConfirmDialog.OnConfirmListener {
    protected boolean isFinish;
    private LoadTipsDialog mLoadTipsDialog;
    private ConfirmDialog mConfirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(getLayoutResId());
        ButterKnife.bind(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFinish = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFinish = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消保持屏幕常亮
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onConfirm(boolean isConfirm) {
    }

    /**
     * 获取布局ID
     *
     * @return
     */
    protected abstract int getLayoutResId();

    /**
     * 初始化
     */
    protected abstract void init();

    protected void showToastTips(final String content) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtils.getInstance(this).show(content);
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                showToastTips(content);
            }
        });
    }

    protected void showLoadTipsDialog(String tips, OnDialogBackEventListener listener) {
        if (!isLoadTipsDialogShow()) {
            mLoadTipsDialog = LoadTipsDialog.newInstance(tips);
            mLoadTipsDialog.setOnDialogBackEventListener(listener);
            mLoadTipsDialog.show(getSupportFragmentManager(), "LOAD_DIALOG");
        }
    }

    protected void closeLoadTipsDialog() {
        if (mLoadTipsDialog != null) {
            try {
                mLoadTipsDialog.dismiss();
                mLoadTipsDialog = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void showConfirmDialog(String tips) {
        if (!isConfirmDialogShow()) {
            mConfirmDialog = ConfirmDialog.newInstance(tips);
            mConfirmDialog.setOnConfirmListener(this);
            mConfirmDialog.show(getSupportFragmentManager(), "TIPS_DIALOG");
        }
    }

    protected void closeConfirmDialog() {
        if (isConfirmDialogShow()) {
            mConfirmDialog.dismiss();
            mConfirmDialog = null;
        }
    }

    private boolean isConfirmDialogShow() {
        return mConfirmDialog != null && mConfirmDialog.getDialog() != null && mConfirmDialog.getDialog().isShowing();
    }

    private boolean isLoadTipsDialogShow() {
        return mLoadTipsDialog != null && mLoadTipsDialog.getDialog() != null && mLoadTipsDialog.getDialog().isShowing();
    }
}
