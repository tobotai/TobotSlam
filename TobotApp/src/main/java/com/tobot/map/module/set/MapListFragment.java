package com.tobot.map.module.set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.common.AbstractLoadMore;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.ListUtils;
import com.tobot.map.util.MediaScanner;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnFinishListener;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author houdeming
 * @date 2019/10/21
 */
public class MapListFragment extends BaseFragment implements DataHelper.MapRequestCallback, MapAdapter.OnMapListener<String> {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_file_catalog)
    TextView tvFileCatalog;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_pull_load_more)
    TextView tvPullLoadMore;
    private static final int MSG_GET_MAP = 1;
    private static final int MSG_MAP_LOAD = 2;
    private MainHandler mMainHandler;
    private MapAdapter mAdapter;
    private List<String> mMapData;
    private String mMapFile;
    private static final int MAP_SWITCH = 0;
    private static final int MAP_DELETE = 1;
    private int mTipsStatus;
    public static final int PAGE_COUNT = 10;
    private int mCurrentIndex;
    private final List<String> mLoadList = new ArrayList<>();

    public static MapListFragment newInstance() {
        return new MapListFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_map_list;
    }

    @Override
    protected void init() {
        tvFileCatalog.setText(getString(R.string.tv_file_catalog, BaseConstant.getMapDirectory(getActivity())));
        mMainHandler = new MainHandler(new WeakReference<>(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new MapAdapter(getActivity(), R.layout.recycler_item_map);
        mAdapter.setOnMapListener(this);
        recyclerView.setAdapter(mAdapter);
        DataHelper.getInstance().requestMapFileList(getActivity(), this);
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
        closeLoadTipsDialog();
        closeConfirmDialog();
    }

    @Override
    public void onMapList(List<String> data) {
        if (mMainHandler != null) {
            mMainHandler.obtainMessage(MSG_GET_MAP, data).sendToTarget();
        }
    }

    @Override
    public void onMapSwitch(int position, String data) {
        if (!SlamManager.getInstance().isConnected()) {
            showToastTips(getString(R.string.slam_not_connect_tips));
            return;
        }

        mMapFile = data;
        mTipsStatus = MAP_SWITCH;
        showConfirmDialog(getString(R.string.tv_map_switch_tips));
    }

    @Override
    public void onMapDelete(int position, String data) {
        mMapFile = data;
        mTipsStatus = MAP_DELETE;
        showConfirmDialog(getString(R.string.tv_map_delete_tips));
    }

    @Override
    public void onConfirm(boolean isConfirm) {
        if (isConfirm) {
            if (mTipsStatus == MAP_SWITCH) {
                showLoadTipsDialog(getString(R.string.map_load_tips));
                DataHelper.getInstance().setCurrentMapFile(mMapFile);
                SlamManager.getInstance().loadMapAsync(BaseConstant.getMapFilePath(getActivity(), mMapFile), new OnFinishListener<List<LocationBean>>() {
                    @Override
                    public void onFinish(List<LocationBean> data) {
                        MyDBSource.getInstance(getActivity()).deleteAllLocation();
                        if (data != null && !data.isEmpty()) {
                            MyDBSource.getInstance(getActivity()).insertLocationList(data);
                        }

                        if (mMainHandler != null) {
                            mMainHandler.obtainMessage(MSG_MAP_LOAD, true).sendToTarget();
                        }
                    }

                    @Override
                    public void onError() {
                        if (mMainHandler != null) {
                            mMainHandler.obtainMessage(MSG_MAP_LOAD, false).sendToTarget();
                        }
                    }
                });
                return;
            }

            // 删除地图
            if (TextUtils.equals(mMapFile, DataHelper.getInstance().getCurrentMapFile())) {
                DataHelper.getInstance().setCurrentMapFile("");
            }
            String filePath = BaseConstant.getMapFilePath(getActivity(), mMapFile);
            SlamManager.getInstance().deleteFile(filePath);
            new MediaScanner().scanFile(getActivity(), filePath);
            mCurrentIndex = 0;
            DataHelper.getInstance().requestMapFileList(getActivity(), this);
            showToastTips(getString(R.string.delete_success));
        }
    }

    private static class MainHandler extends Handler {
        private final MapListFragment mFragment;

        private MainHandler(WeakReference<MapListFragment> reference) {
            super(Looper.getMainLooper());
            mFragment = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mFragment == null) {
                return;
            }

            switch (msg.what) {
                case MSG_GET_MAP:
                    mFragment.updateRecyclerView((List<String>) msg.obj);
                    break;
                case MSG_MAP_LOAD:
                    mFragment.handleMapLoadResult((boolean) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    private void updateRecyclerView(List<String> data) {
        mMapData = data;
        if (mAdapter != null) {
            mCurrentIndex++;
            if (!mLoadList.isEmpty()) {
                mLoadList.clear();
            }

            List<String> list = ListUtils.page(data, mCurrentIndex, PAGE_COUNT);
            if (list != null && !list.isEmpty()) {
                mLoadList.addAll(list);
            }
            mAdapter.setCurrentMap(DataHelper.getInstance().getCurrentMapFile());
            mAdapter.setData(mLoadList);
            showLoadTips(data);
        }
    }

    private void handleLoadMore(int lastItem) {
        mCurrentIndex++;
        List<String> list = ListUtils.page(mMapData, mCurrentIndex, PAGE_COUNT);
        if (list != null && !list.isEmpty()) {
            mLoadList.addAll(list);
            mAdapter.setData(mLoadList);
        }
        showLoadTips(mMapData);
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

    private void handleMapLoadResult(boolean isSuccess) {
        closeLoadTipsDialog();
        if (isSuccess) {
            showToastTips(getString(R.string.map_load_success_tips));
            Activity activity = getActivity();
            if (activity != null) {
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
            }
            return;
        }

        showToastTips(getString(R.string.map_load_fail_tips));
    }
}
