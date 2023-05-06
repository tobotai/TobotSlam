package com.tobot.map.module.set.firmware;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.common.AbstractLoadMore;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.common.TipsDialog;
import com.tobot.map.module.log.Logger;
import com.tobot.map.util.FileUtils;
import com.tobot.map.util.ListUtils;
import com.tobot.map.util.MediaScanner;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnUpgradeListener;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author houdeming
 * @date 2022/09/14
 */
public abstract class AbstractFirmwareFragment extends BaseFragment implements Runnable, UpgradeFileAdapter.OnUpgradeListener<String>, OnUpgradeListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_file_catalog)
    TextView tvTips;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_pull_load_more)
    TextView tvPullLoadMore;
    private static final int MSG_GET_FILE = 1;
    private static final int MSG_UPDATE_PROGRESS = 2;
    private static final int MSG_UPDATE_RESULT = 3;
    private MainHandler mMainHandler;
    private UpgradeFileAdapter mAdapter;
    private static final int UPGRADE = 0;
    private static final int DELETE = 1;
    private int mTipsStatus;
    private String mDir, mFileName;
    private ProgressDialog mProgressDialog;
    private TipsDialog mTipsDialog;
    public static final int PAGE_COUNT = 10;
    private int mCurrentIndex;
    private List<String> mAllList;
    private final List<String> mLoadList = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_map_list;
    }

    @Override
    protected void init() {
        mDir = getFirmwareDirectory(getActivity());
        tvTips.setText(getString(R.string.tv_file_catalog, mDir));
        mMainHandler = new MainHandler(new WeakReference<>(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new UpgradeFileAdapter(getActivity(), R.layout.recycler_item_upgrade_file);
        mAdapter.setOnUpgradeListener(this);
        recyclerView.setAdapter(mAdapter);
        ThreadPoolManager.getInstance().execute(this);
        recyclerView.addOnScrollListener(new AbstractLoadMore() {

            @Override
            protected void onLoadMore(int lastItem) {
                handleLoadMore(lastItem);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
        closeConfirmDialog();
        closeProgressDialog();
        if (isTipsDialogShow()) {
            mTipsDialog.getDialog().dismiss();
            mTipsDialog = null;
        }
    }

    @Override
    public void run() {
        List<String> data = FileUtils.getFileNameList(mDir, BaseConstant.FILE_FIRMWARE_NAME_SUFFIX);
        if (mMainHandler != null) {
            mMainHandler.obtainMessage(MSG_GET_FILE, data).sendToTarget();
        }
    }

    @Override
    public void onUpgrade(int position, String data) {
        if (!SlamManager.getInstance().isConnected()) {
            showToastTips(getString(R.string.slam_not_connect_tips));
            return;
        }

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
                new MediaScanner().scanFile(getActivity(), filePath);
                updateData();
                showToastTips(getString(R.string.delete_success));
                return;
            }

            // 升级
            handleUpgrade(filePath);
        }
    }

    @Override
    public void onUpgradeProgress(int progress) {
        if (getActivity() != null && mMainHandler != null) {
            mMainHandler.obtainMessage(MSG_UPDATE_PROGRESS, progress, 0).sendToTarget();
        }
    }

    @Override
    public void onUpgradeResult(boolean isSuccess) {
        Logger.i(BaseConstant.TAG, "firmware upgrade isSuccess=" + isSuccess);
        if (getActivity() != null && mMainHandler != null) {
            mMainHandler.obtainMessage(MSG_UPDATE_RESULT, isSuccess).sendToTarget();
        }
    }

    /**
     * 获取固件目录
     *
     * @param context
     * @return
     */
    protected abstract String getFirmwareDirectory(Context context);

    /**
     * 升级
     *
     * @param filePath
     */
    protected abstract void handleUpgrade(String filePath);

    public void updateData() {
        mCurrentIndex = 0;
        ThreadPoolManager.getInstance().execute(this);
    }

    private void updateRecyclerView(List<String> data) {
        mAllList = data;
        if (mAdapter != null) {
            mCurrentIndex++;
            if (!mLoadList.isEmpty()) {
                mLoadList.clear();
            }

            List<String> list = ListUtils.page(data, mCurrentIndex, PAGE_COUNT);
            if (list != null && !list.isEmpty()) {
                mLoadList.addAll(list);
            }
            mAdapter.setData(mLoadList);
            showLoadTips(data);
        }
    }

    private void handleLoadMore(int lastItem) {
        mCurrentIndex++;
        List<String> list = ListUtils.page(mAllList, mCurrentIndex, PAGE_COUNT);
        if (list != null && !list.isEmpty()) {
            mLoadList.addAll(list);
            mAdapter.setData(mLoadList);
        }
        showLoadTips(mAllList);
    }

    private void showLoadTips(List<String> data) {
        if (data != null && !data.isEmpty()) {
            int size = data.size();
            if (size > PAGE_COUNT && mCurrentIndex * PAGE_COUNT < size) {
                tvPullLoadMore.setVisibility(View.VISIBLE);
                return;
            }
        }

        tvPullLoadMore.setVisibility(View.GONE);
    }

    protected void showProgressDialog(int progress) {
        if (isProgressDialogShow()) {
            mProgressDialog.updateTips(progress);
            return;
        }

        mProgressDialog = ProgressDialog.newInstance(getString(R.string.upgrade_ing));
        FragmentManager fragmentManager = getFragmentManager();
        if (fragmentManager != null) {
            mProgressDialog.show(fragmentManager, "PROGRESS_DIALOG");
        }
    }

    private void upgradeResult(boolean isSuccess) {
        closeProgressDialog();
        if (!isTipsDialogShow()) {
            String tips = getString(isSuccess ? R.string.firmware_upgrade_success_tips : R.string.firmware_upgrade_fail_tips);
            mTipsDialog = TipsDialog.newInstance(tips);
            mTipsDialog.setOnConfirmListener(new TipsDialog.OnConfirmListener() {
                @Override
                public void onConfirm() {
                    // 退出应用
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.setResult(BaseConstant.CODE_EXIT);
                        activity.finish();
                    }
                }
            });

            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                mTipsDialog.show(fragmentManager, "TIPS_DIALOG");
            }
        }
    }

    private void closeProgressDialog() {
        if (isProgressDialogShow()) {
            mProgressDialog.getDialog().dismiss();
            mProgressDialog = null;
        }
    }

    private boolean isProgressDialogShow() {
        return mProgressDialog != null && mProgressDialog.getDialog() != null && mProgressDialog.getDialog().isShowing();
    }

    private boolean isTipsDialogShow() {
        return mTipsDialog != null && mTipsDialog.getDialog() != null && mTipsDialog.getDialog().isShowing();
    }

    private static class MainHandler extends Handler {
        private final AbstractFirmwareFragment mFragment;

        private MainHandler(WeakReference<AbstractFirmwareFragment> reference) {
            super(Looper.getMainLooper());
            mFragment = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mFragment != null) {
                switch (msg.what) {
                    case MSG_GET_FILE:
                        mFragment.updateRecyclerView((List<String>) msg.obj);
                        break;
                    case MSG_UPDATE_PROGRESS:
                        mFragment.showProgressDialog(msg.arg1);
                        break;
                    case MSG_UPDATE_RESULT:
                        mFragment.upgradeResult((Boolean) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
