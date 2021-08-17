package com.tobot.map.module.set;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.common.NameInputDialog;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.MediaScanner;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;
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
public class MapListFragment extends BaseFragment implements DataHelper.MapRequestCallBack, MapAdapter.OnMapListener<String>, NameInputDialog.OnNameListener {
    private static final int MSG_GET_MAP = 1;
    private static final int MSG_MAP_LOAD = 2;
    private static final long TIME_SWITCH_MAP_DELAY = 3 * 1000;
    @BindView(R.id.tv_file_catalog)
    TextView tvFileCatalog;
    @BindView(R.id.recycler_map)
    RecyclerView recyclerView;
    private MainHandler mMainHandler;
    private MapAdapter mAdapter;
    private List<String> mMapData;
    private NameInputDialog mNameInputDialog;
    private String mMapName;
    private static final int MAP_SWITCH = 0;
    private static final int MAP_DELETE = 1;
    private int mTipsStatus;

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
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }

        closeNameInputDialog();
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
            showToastTips(getString(R.string.slam_connect_not_tips));
            return;
        }

        mMapName = data;
        mTipsStatus = MAP_SWITCH;
        showConfirmDialog(getString(R.string.tv_map_switch_tips));
    }

    @Override
    public void onMapEdit(int position, String data) {
        if (!isNameInputDialogShow()) {
            mMapName = data;
            mNameInputDialog = NameInputDialog.newInstance(getString(R.string.tv_title_edit_map), getString(R.string.map_edit_rule_tips), getString(R.string.et_hint_edit_map_tips));
            mNameInputDialog.setOnNameListener(this);
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                mNameInputDialog.show(fragmentManager, "MAP_EDIT_DIALOG");
            }
        }
    }

    @Override
    public void onMapDelete(int position, String data) {
        mMapName = data;
        mTipsStatus = MAP_DELETE;
        showConfirmDialog(getString(R.string.tv_map_delete_tips));
    }

    @Override
    public void onConfirm() {
        super.onConfirm();
        if (mTipsStatus == MAP_SWITCH) {
            showLoadTipsDialog(getString(R.string.map_load_tips));
            DataHelper.getInstance().setCurrentMapName(mMapName);
            SlamManager.getInstance().loadMapAsync(BaseConstant.getMapNamePath(getActivity(), mMapName), new OnFinishListener<List<LocationBean>>() {
                @Override
                public void onFinish(List<LocationBean> data) {
                    MyDBSource.getInstance(getActivity()).deleteAllLocation();
                    List<LocationBean> sensorList = new ArrayList<>();
                    if (data != null && !data.isEmpty()) {
                        MyDBSource.getInstance(getActivity()).insertLocationList(data);
                        // 设置传感区域
                        for (LocationBean bean : data) {
                            // 只获取超声波关闭状态的位置点
                            if (bean.getSensorStatus() == SlamCode.ULTRASONIC_STATUS_CLOSE) {
                                if (bean.getStartX() != 0 || bean.getEndX() != 0) {
                                    sensorList.add(bean);
                                }
                            }
                        }
                    }

                    SlamManager.getInstance().setSensorArea(sensorList);
                    // 这里必须要做延时处理，不然不能重定位成功
                    if (mMainHandler != null) {
                        mMainHandler.sendMessageDelayed(mMainHandler.obtainMessage(MSG_MAP_LOAD, true), TIME_SWITCH_MAP_DELAY);
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
        if (TextUtils.equals(mMapName, DataHelper.getInstance().getCurrentMapName())) {
            DataHelper.getInstance().setCurrentMapName("");
        }
        String filePath = BaseConstant.getMapNamePath(getActivity(), mMapName);
        SlamManager.getInstance().deleteFile(filePath);
        new MediaScanner().scanFile(getActivity(), filePath);
        DataHelper.getInstance().requestMapFileList(getActivity(), this);
    }

    @Override
    public void onName(String name) {
        if (TextUtils.isEmpty(name)) {
            ToastUtils.getInstance(getActivity()).show(R.string.map_name_empty_tips);
            return;
        }

        if (isHasExitMap(name)) {
            showToastTips(getString(R.string.map_exist_tips));
            return;
        }

        closeNameInputDialog();
        // 只是更改了地图的文件名字，文件内容并没有改变
        String oldPath = BaseConstant.getMapNamePath(getActivity(), mMapName);
        String newPath = BaseConstant.getMapNumPath(getActivity(), name);
        boolean isSuccess = SlamManager.getInstance().renameFile(oldPath, newPath);
        if (isSuccess) {
            showToastTips(getString(R.string.map_edit_success_tips));
            new MediaScanner().scanFile(getActivity(), new String[]{oldPath, newPath});
            DataHelper.getInstance().requestMapFileList(getActivity(), this);
            return;
        }

        showToastTips(getString(R.string.map_edit_fail_tips));
    }

    private static class MainHandler extends Handler {
        private MapListFragment mFragment;

        private MainHandler(WeakReference<MapListFragment> reference) {
            super(Looper.getMainLooper());
            mFragment = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_GET_MAP:
                    if (mFragment != null) {
                        mFragment.updateRecyclerView((List<String>) msg.obj);
                    }
                    break;
                case MSG_MAP_LOAD:
                    if (mFragment != null) {
                        mFragment.handleMapLoadResult((boolean) msg.obj);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void updateRecyclerView(List<String> data) {
        mMapData = data;
        if (mAdapter != null) {
            mAdapter.setCurrentMap(DataHelper.getInstance().getCurrentMapName());
            mAdapter.setData(data);
        }
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

    private boolean isHasExitMap(String number) {
        return mMapData != null && !mMapData.isEmpty() && mMapData.contains(BaseConstant.getMapFileName(number));
    }

    private void closeNameInputDialog() {
        if (isNameInputDialogShow()) {
            mNameInputDialog.getDialog().dismiss();
            mNameInputDialog = null;
        }
    }

    private boolean isNameInputDialogShow() {
        return mNameInputDialog != null && mNameInputDialog.getDialog() != null && mNameInputDialog.getDialog().isShowing();
    }
}
