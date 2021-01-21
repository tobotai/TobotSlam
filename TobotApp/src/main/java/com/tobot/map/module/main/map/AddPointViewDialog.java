package com.tobot.map.module.main.map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.slamtec.slamware.robot.Pose;
import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.common.BaseAnimDialog;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.LogUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.data.LocationBean;

import java.util.List;

/**
 * @author houdeming
 * @date 2019/10/22
 */
public class AddPointViewDialog extends BaseAnimDialog implements View.OnClickListener, LocationAdapter.OnLocationListener {
    private Button btnSort;
    private LocationAdapter mAdapter;
    private OnPointListener mOnPointListener;
    private LocationBean mLocationBean;
    private Pose mPose;
    private List<LocationBean> mLocationList;

    public static AddPointViewDialog newInstance() {
        return new AddPointViewDialog();
    }

    @Override
    protected int getAnimId() {
        return R.style.add_point_view_dialog_anim;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_add_point_view;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    protected int getGravity() {
        return Gravity.LEFT;
    }

    @Override
    protected int getDialogWidth() {
        return getResources().getDimensionPixelSize(R.dimen.dialog_add_point_view_width);
    }

    @Override
    protected int getDialogHeight() {
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void initView(View view) {
        view.findViewById(R.id.btn_add_current_point).setOnClickListener(this);
        btnSort = view.findViewById(R.id.btn_sort_point);
        btnSort.setOnClickListener(this);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_point);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new LocationAdapter(getActivity(), R.layout.recycler_item_location);
        mAdapter.setOnLocationListener(this);
        recyclerView.setAdapter(mAdapter);
        ThreadPoolManager.getInstance().execute(new DataRunnable());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.i("AddPointViewDialog requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (resultCode == Activity.RESULT_OK && data != null) {
            showLocationData(MyDBSource.getInstance(getActivity()).queryLocation());
            if (mOnPointListener != null) {
                LocationBean bean = data.getParcelableExtra(BaseConstant.DATA_KEY);
                mOnPointListener.onUpdateLocationLabel(data.getStringExtra(BaseConstant.NUMBER_KEY), bean);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_add_current_point) {
            mPose = null;
            // 请求当前pose
            ThreadPoolManager.getInstance().execute(new PoseRunnable());
            showNameInputDialog(getString(R.string.tv_title_add_location), getString(R.string.name_rule_tips), getString(R.string.et_hint_location_tips));
            return;
        }

        if (id == R.id.btn_sort_point) {
            List<LocationBean> sortList = DataHelper.getInstance().sortPoint(mLocationList);
            if (sortList != null && !sortList.isEmpty()) {
                MyDBSource.getInstance(getActivity()).deleteAllLocation();
                MyDBSource.getInstance(getActivity()).insertLocationList(sortList);
            }
            ToastUtils.getInstance(getActivity()).show(getString(R.string.point_sort_success));
            showLocationData(sortList);
        }
    }

    @Override
    public void onName(String name) {
        if (TextUtils.isEmpty(name)) {
            ToastUtils.getInstance(getActivity()).show(getString(R.string.name_empty_tips));
            return;
        }
        if (MyDBSource.getInstance(getActivity()).queryLocation(name) != null) {
            ToastUtils.getInstance(getActivity()).show(getString(R.string.number_edit_fail_tips));
            return;
        }
        if (mPose == null) {
            ToastUtils.getInstance(getActivity()).show(getString(R.string.pose_get_fail_tips));
            ThreadPoolManager.getInstance().execute(new PoseRunnable());
            return;
        }
        closeNameInputDialog();
        LocationBean bean = new LocationBean();
        bean.setLocationNumber(name);
        bean.setX(mPose.getX());
        bean.setY(mPose.getY());
        bean.setYaw(mPose.getYaw());
        MyDBSource.getInstance(getActivity()).insertLocation(bean);
        showLocationData(MyDBSource.getInstance(getActivity()).queryLocation());
        if (mOnPointListener != null) {
            mOnPointListener.onAddLocationLabel(bean);
        }
    }

    @Override
    public void onMoveTo(LocationBean data) {
        if (mOnPointListener != null) {
            mOnPointListener.onMoveTo(data);
        }
    }

    @Override
    public void onEditLocation(LocationBean data, int position) {
        if (data != null) {
            Intent intent = new Intent(getActivity(), LocationEditActivity.class);
            intent.putExtra(BaseConstant.DATA_KEY, data);
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onDeleteLocation(LocationBean data, int position) {
        if (data != null) {
            mLocationBean = data;
            showConfirmDialog(getString(R.string.tv_location_delete_tips));
        }
    }

    @Override
    public void onConfirm() {
        // 删除确认的操作
        if (mLocationBean != null) {
            String number = mLocationBean.getLocationNumber();
            MyDBSource.getInstance(getActivity()).deleteLocation(number);
            showLocationData(MyDBSource.getInstance(getActivity()).queryLocation());
            if (mOnPointListener != null) {
                mOnPointListener.onDeleteLocationLabel(number);
            }
        }
    }

    public void setOnPointListener(OnPointListener listener) {
        mOnPointListener = listener;
    }

    private void showLocationData(List<LocationBean> data) {
        mLocationList = data;
        if (data != null && !data.isEmpty()) {
            if (btnSort.getVisibility() != View.VISIBLE) {
                btnSort.setVisibility(View.VISIBLE);
            }
        } else {
            if (btnSort.getVisibility() == View.VISIBLE) {
                btnSort.setVisibility(View.GONE);
            }
        }

        if (mAdapter != null) {
            mAdapter.setData(data);
        }
    }

    private class DataRunnable implements Runnable {
        @Override
        public void run() {
            List<LocationBean> data = MyDBSource.getInstance(getActivity()).queryLocation();
            if (getActivity() == null) {
                return;
            }
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLocationData(data);
                }
            });
        }
    }

    private class PoseRunnable implements Runnable {
        @Override
        public void run() {
            mPose = SlamManager.getInstance().getPose();
        }
    }

    public interface OnPointListener {
        /**
         * 导航
         *
         * @param data
         */
        void onMoveTo(LocationBean data);

        /**
         * 添加位置坐标
         *
         * @param data
         */
        void onAddLocationLabel(LocationBean data);

        /**
         * 更新坐标的位置
         *
         * @param oldNumber
         * @param data
         */
        void onUpdateLocationLabel(String oldNumber, LocationBean data);

        /**
         * 删除坐标
         *
         * @param number
         */
        void onDeleteLocationLabel(String number);
    }
}
