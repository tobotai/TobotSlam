package com.tobot.map.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tobot.map.module.common.ConfirmDialog;
import com.tobot.map.module.common.LoadTipsDialog;
import com.tobot.map.util.ToastUtils;

import butterknife.ButterKnife;

/**
 * @author houdeming
 * @date 2018/7/20
 */
public abstract class BaseFragment extends Fragment implements ConfirmDialog.OnConfirmListener {
    private LoadTipsDialog mLoadTipsDialog;
    private ConfirmDialog mConfirmDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        ButterKnife.bind(this, view);
        init();
        return view;
    }

    @Override
    public void onConfirm() {
    }

    /**
     * 获取资源ID
     *
     * @return
     */
    protected abstract int getLayoutId();

    /**
     * 初始化
     */
    protected abstract void init();

    protected void showToastTips(final String content) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtils.getInstance(getActivity()).show(content);
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                showToastTips(content);
            }
        });
    }

    protected void showLoadTipsDialog(String tips) {
        if (!isLoadTipsDialogShow()) {
            mLoadTipsDialog = LoadTipsDialog.newInstance(tips);
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                mLoadTipsDialog.show(fragmentManager, "LOAD_DIALOG");
            }
        }
    }

    protected void closeLoadTipsDialog() {
        if (isLoadTipsDialogShow()) {
            mLoadTipsDialog.getDialog().dismiss();
            mLoadTipsDialog = null;
        }
    }

    protected void showConfirmDialog(String tips) {
        if (!isConfirmDialogShow()) {
            mConfirmDialog = ConfirmDialog.newInstance(tips);
            mConfirmDialog.setOnConfirmListener(this);
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                mConfirmDialog.show(fragmentManager, "TIPS_DIALOG");
            }
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
