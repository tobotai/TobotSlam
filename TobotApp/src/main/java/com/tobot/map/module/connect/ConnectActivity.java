package com.tobot.map.module.connect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.event.CheckEndEvent;
import com.tobot.map.event.ConnectSlamEvent;
import com.tobot.map.event.ConnectSuccessEvent;
import com.tobot.map.event.DownloadEvent;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.common.PermissionHelper;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.module.main.MainActivity;
import com.tobot.map.module.main.MapService;
import com.tobot.map.module.set.SetActivity;
import com.tobot.map.module.upgrade.UpgradeTipsDialog;
import com.tobot.map.util.AppUtils;
import com.tobot.map.util.NetworkUtils;
import com.tobot.map.util.SystemUtils;
import com.tobot.slam.SlamManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2019/10/18
 */
public class ConnectActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemClickListener<String>, UpgradeTipsDialog.OnUpgradeListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_wifi)
    TextView tvWifi;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.et_ip_input)
    EditText etIp;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_ip)
    RecyclerView recyclerView;
    private static final int CODE_FILES_PERMISSION = 1;
    private ConnectIpAdapter mAdapter;
    private List<String> mIpList;
    private UpgradeTipsDialog mUpgradeTipsDialog;
    private String mIp;
    private boolean isPause, isBackEvent;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_connect;
    }

    @Override
    protected void init() {
        Logger.i(BaseConstant.TAG, "version=" + AppUtils.getVersion(this, getPackageName()));
        SystemUtils.requestDisplayInfo(this);
        Logger.i(BaseConstant.TAG, "valueSize=" + getResources().getInteger(R.integer.base_values));
        BaseConstant.isSpeedFast = false;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(this, ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new ConnectIpAdapter(this, R.layout.recycler_item_ip);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        String wifiName = NetworkUtils.getWifiName(this);
        tvWifi.setText(getString(R.string.tv_wifi, TextUtils.isEmpty(wifiName) ? getString(R.string.unknown) : wifiName));
        List<String> dataList = MyDBSource.getInstance(this).queryIpList();
        mIpList = dataList;
        if (dataList != null && !dataList.isEmpty()) {
            mAdapter.setData(dataList);
        }
        // 默认没有查看过任何地图
        DataHelper.getInstance().setCurrentMapFile("");
        SlamManager.getInstance().setRelocationQualityMin(DataHelper.getInstance().getRelocationQualityMin(this));
        SlamManager.getInstance().setRelocationQualitySafe(DataHelper.getInstance().getRelocationQualitySafe(this));
        SlamManager.getInstance().setRelocationAreaRadius(DataHelper.getInstance().getRelocationAreaRadius(this));
        // 底盘半径默认0.25米，可以设置不同大小底盘
        SlamManager.getInstance().setChassisRadius(DataHelper.getInstance().getChassisRadius(this));
        EventBus.getDefault().register(this);
        startService(new Intent(getApplicationContext(), MapService.class));
        // Android11以上版本请求读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            boolean isExternalStorageManager = Environment.isExternalStorageManager();
            Logger.i(BaseConstant.TAG, "isExternalStorageManager=" + isExternalStorageManager);
            if (!isExternalStorageManager) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, CODE_FILES_PERMISSION);
                return;
            }
        }

        PermissionHelper.checkPermission(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_FILES_PERMISSION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            boolean isExternalStorageManager = Environment.isExternalStorageManager();
            Logger.i(BaseConstant.TAG, "onActivityResult() isExternalStorageManager=" + isExternalStorageManager);
            if (isExternalStorageManager) {
                PermissionHelper.checkPermission(this);
                return;
            }

            showToastTips(getString(R.string.file_permission_fail_tips));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPause = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        closeLoadTipsDialog();
        if (isUpgradeTipsDialogShow()) {
            mUpgradeTipsDialog.dismiss();
            mUpgradeTipsDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isBackEvent = true;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.i(BaseConstant.TAG, "onDestroy()");
        EventBus.getDefault().unregister(this);
        // 只有按返回键退出才关闭
        if (isBackEvent) {
            stopService(new Intent(getApplicationContext(), MapService.class));
        }
    }

    @Override
    public void onItemClick(int position, String data) {
        etIp.setText(data);
        connect(data);
    }

    @Override
    public void onUpgrade(boolean isUpgrade) {
        if (isUpgrade) {
            showToastTips(getString(R.string.tv_apk_download_begin));
            EventBus.getDefault().post(new DownloadEvent());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.iv_set, R.id.btn_sta_connect, R.id.tv_delete})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.iv_set:
                startActivity(new Intent(this, SetActivity.class));
                break;
            case R.id.btn_sta_connect:
                String ip = etIp.getText().toString().trim();
                if (TextUtils.isEmpty(ip)) {
                    showToastTips(getString(R.string.ip_empty_tips));
                    return;
                }

                connect(ip);
                break;
            case R.id.tv_delete:
                if (mIpList != null && !mIpList.isEmpty()) {
                    mIpList.clear();
                    MyDBSource.getInstance(this).deleteAllIp();
                    mAdapter.setData(mIpList);
                    showToastTips(getString(R.string.delete_success));
                }
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCheckEndEvent(CheckEndEvent event) {
        if (!isPause && event.isUpgrade()) {
            if (!isUpgradeTipsDialogShow()) {
                mUpgradeTipsDialog = UpgradeTipsDialog.newInstance();
                mUpgradeTipsDialog.setOnUpgradeListener(this);
                mUpgradeTipsDialog.show(getSupportFragmentManager(), "UPGRADE_TIPS_DIALOG");
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectSuccessEvent(ConnectSuccessEvent event) {
        DataHelper.getInstance().setIp(mIp);
        startActivity(new Intent(this, MainActivity.class));
        isBackEvent = false;
        finish();
    }

    private void connect(String ip) {
        mIp = ip;
        SystemUtils.hideKeyboard(this);
        showLoadTipsDialog(getString(R.string.connect_ing), null);
        EventBus.getDefault().post(new ConnectSlamEvent(ip));
    }

    private boolean isUpgradeTipsDialogShow() {
        return mUpgradeTipsDialog != null && mUpgradeTipsDialog.getDialog() != null && mUpgradeTipsDialog.getDialog().isShowing();
    }
}
