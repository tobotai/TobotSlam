package com.tobot.map.module.set.firmware;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.common.TipsDialog;
import com.tobot.map.module.log.Logger;
import com.tobot.map.util.FileUtils;
import com.tobot.map.util.MediaScanner;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnUpgradeListener;

import java.io.File;

import butterknife.BindView;

/**
 * @author houdeming
 * @date 2021/03/11
 */
public class FirmwareUpgradeActivity extends BaseActivity implements UpgradeFileAdapter.OnUpgradeListener<String>, OnUpgradeListener {
    @BindView(R.id.tv_head)
    TextView tvHead;
    @BindView(R.id.tv_file_tips)
    TextView tvTips;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private UpgradeFileAdapter mAdapter;
    private static final int UPGRADE = 0;
    private static final int DELETE = 1;
    private int mTipsStatus;
    private String mDir, mFileName;
    private UpgradeProgressDialog mUpgradeProgressDialog;
    private TipsDialog mTipsDialog;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_firmware_upgrade;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.firmware_upgrade);
        mDir = BaseConstant.getFirmwareDirectory(this);
        tvTips.setText(getString(R.string.tv_file_catalog, mDir));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(this, ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new UpgradeFileAdapter(this, R.layout.recycler_item_upgrade_file);
        mAdapter.setOnUpgradeListener(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setData(FileUtils.getFileNameList(mDir, BaseConstant.FILE_FIRMWARE_NAME_SUFFIX));
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeConfirmDialog();
        closeUpgradeProgressDialog();
        if (isTipsDialogShow()) {
            mTipsDialog.getDialog().dismiss();
            mTipsDialog = null;
        }
    }

    @Override
    public void onUpgrade(int position, String data) {
        mFileName = data;
        mTipsStatus = UPGRADE;
        showConfirmDialog(getString(R.string.tv_upgrade_file_tips, data));
        if (mAdapter != null) {
            mAdapter.setSelectIndex(position);
        }
    }

    @Override
    public void onDelete(int position, String data) {
        mFileName = data;
        mTipsStatus = DELETE;
        showConfirmDialog(getString(R.string.tv_upgrade_file_delete_tips, data));
    }

    @Override
    public void onConfirm(boolean isConfirm) {
        if (isConfirm) {
            String filePath = mDir + File.separator + mFileName;
            // 删除
            if (mTipsStatus == DELETE) {
                FileUtils.deleteFile(filePath);
                new MediaScanner().scanFile(this, filePath);
                mAdapter.setData(FileUtils.getFileNameList(mDir, BaseConstant.FILE_FIRMWARE_NAME_SUFFIX));
                showToastTips(getString(R.string.delete_success));
                return;
            }

            // 升级
            showUpgradeProgressDialog();
            SlamManager.getInstance().upgradeControlPanelAsync(filePath, this);
        }
    }

    @Override
    public void onUpgradeProgress(int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isUpgradeProgressDialogShow()) {
                    mUpgradeProgressDialog.updateTips(progress);
                    return;
                }

                showUpgradeProgressDialog();
            }
        });
    }

    @Override
    public void onUpgradeResult(boolean isSuccess) {
        Logger.i(BaseConstant.TAG, "firmware upgrade isSuccess=" + isSuccess);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeUpgradeProgressDialog();
                if (!isTipsDialogShow()) {
                    String tips = getString(isSuccess ? R.string.firmware_upgrade_success_tips : R.string.firmware_upgrade_fail_tips);
                    mTipsDialog = TipsDialog.newInstance(tips);
                    mTipsDialog.setOnConfirmListener(new TipsDialog.OnConfirmListener() {
                        @Override
                        public void onConfirm() {
                            // 退出应用
                            setResult(BaseConstant.CODE_EXIT);
                            finish();
                        }
                    });

                    mTipsDialog.show(getSupportFragmentManager(), "TIPS_DIALOG");
                }
            }
        });
    }

    private void showUpgradeProgressDialog() {
        if (!isUpgradeProgressDialogShow()) {
            mUpgradeProgressDialog = UpgradeProgressDialog.newInstance();
            mUpgradeProgressDialog.show(getSupportFragmentManager(), "UPGRADE_PROGRESS_DIALOG");
        }
    }

    private void closeUpgradeProgressDialog() {
        if (isUpgradeProgressDialogShow()) {
            mUpgradeProgressDialog.getDialog().dismiss();
            mUpgradeProgressDialog = null;
        }
    }

    private boolean isUpgradeProgressDialogShow() {
        return mUpgradeProgressDialog != null && mUpgradeProgressDialog.getDialog() != null && mUpgradeProgressDialog.getDialog().isShowing();
    }

    private boolean isTipsDialogShow() {
        return mTipsDialog != null && mTipsDialog.getDialog() != null && mTipsDialog.getDialog().isShowing();
    }
}
