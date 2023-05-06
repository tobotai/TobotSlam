package com.tobot.map.module.set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.event.CheckEndEvent;
import com.tobot.map.event.CheckEvent;
import com.tobot.map.event.DownloadEvent;
import com.tobot.map.module.common.PermissionHelper;
import com.tobot.map.module.upgrade.UpgradeTipsDialog;
import com.tobot.map.util.AppUtils;
import com.tobot.map.util.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2019/10/21
 */
public class AboutAppFragment extends BaseFragment implements UpgradeTipsDialog.OnUpgradeListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_version)
    TextView tvVersion;
    private UpgradeTipsDialog mUpgradeTipsDialog;
    private boolean isActiveCheckUpgrade;

    public static AboutAppFragment newInstance() {
        return new AboutAppFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_about_app;
    }

    @Override
    protected void init() {
        EventBus.getDefault().register(this);
        Activity activity = getActivity();
        if (activity != null) {
            tvVersion.setText(getString(R.string.tv_version, getString(R.string.app_name), AppUtils.getVersion(activity, activity.getPackageName())));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeLoadTipsDialog();
        if (isUpgradeTipsDialogShow()) {
            mUpgradeTipsDialog.dismiss();
            mUpgradeTipsDialog = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onUpgrade(boolean isUpgrade) {
        if (isUpgrade) {
            showToastTips(getString(R.string.tv_apk_download_begin));
            EventBus.getDefault().post(new DownloadEvent());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.rl_version_update, R.id.rl_contact_us})
    public void onClickView(View v) {
        switch (v.getId()) {
            case R.id.rl_version_update:
                versionUpdate();
                break;
            case R.id.rl_contact_us:
                startActivity(new Intent(getActivity(), ContactUsActivity.class));
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckEndEvent(CheckEndEvent event) {
        // 避免主动监测升级的时候，在当前页面也提示
        if (!isActiveCheckUpgrade) {
            return;
        }

        closeLoadTipsDialog();
        if (event.isUpgrade()) {
            if (!isUpgradeTipsDialogShow()) {
                mUpgradeTipsDialog = UpgradeTipsDialog.newInstance();
                mUpgradeTipsDialog.setOnUpgradeListener(this);
                FragmentManager fragmentManager = getFragmentManager();
                if (fragmentManager != null) {
                    mUpgradeTipsDialog.show(fragmentManager, "UPGRADE_DIALOG");
                }
            }
            return;
        }

        // 无新版本
        showToastTips(getString(R.string.version_latest));
    }

    private void versionUpdate() {
        if (NetworkUtils.isConnected(getActivity())) {
            // 读写文件需要SD卡权限
            if (PermissionHelper.isRequestPermission(getActivity())) {
                showToastTips(getString(R.string.permission_sd_card_open_tips));
                return;
            }

            showLoadTipsDialog(getString(R.string.tv_check_version_tips));
            isActiveCheckUpgrade = true;
            EventBus.getDefault().post(new CheckEvent());
            return;
        }

        showToastTips(getString(R.string.net_not_connect_tips));
    }

    private boolean isUpgradeTipsDialogShow() {
        return mUpgradeTipsDialog != null && mUpgradeTipsDialog.getDialog() != null && mUpgradeTipsDialog.getDialog().isShowing();
    }
}
