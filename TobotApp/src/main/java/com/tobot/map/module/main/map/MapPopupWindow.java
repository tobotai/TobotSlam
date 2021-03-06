package com.tobot.map.module.main.map;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.common.BasePopupWindow;
import com.tobot.map.module.common.ConfirmDialog;
import com.tobot.map.module.common.LoadTipsDialog;
import com.tobot.map.module.common.NameInputDialog;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.module.main.MainActivity;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnResultListener;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * 地图
 *
 * @author houdeming
 * @date 2020/3/15
 */
public class MapPopupWindow extends BasePopupWindow implements PopupWindow.OnDismissListener, NameInputDialog.OnNameListener, ConfirmDialog.OnConfirmListener {
    private MainActivity mActivity;
    private TextView tvBuildMap;
    private OnMapListener mOnMapListener;
    private AddPointViewDialog mAddPointViewDialog;
    private ResetChargeDialog mResetChargeDialog;
    private NameInputDialog mNameInputDialog;
    private LoadTipsDialog mLoadTipsDialog;
    private ConfirmDialog mConfirmDialog;

    public MapPopupWindow(Context context, WeakReference<MainActivity> reference, OnMapListener listener) {
        super(context);
        mActivity = reference.get();
        mOnMapListener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.popup_map;
    }

    @Override
    public void initView(View view) {
        tvBuildMap = view.findViewById(R.id.tv_build_map);
        tvBuildMap.setOnClickListener(this);
        view.findViewById(R.id.tv_add_point).setOnClickListener(this);
        view.findViewById(R.id.tv_reset_charge).setOnClickListener(this);
        view.findViewById(R.id.tv_relocation).setOnClickListener(this);
        view.findViewById(R.id.tv_clean_map).setOnClickListener(this);
        view.findViewById(R.id.tv_save_map).setOnClickListener(this);
    }

    @Override
    public void onDismiss() {
        closeAddPointViewDialog();
        closeNumberInputDialog();
        closeLoadTipsDialog();
        if (isConfirmDialogShow()) {
            mConfirmDialog.getDialog().dismiss();
            mConfirmDialog = null;
        }
        if (isResetChargeDialogShow()) {
            mResetChargeDialog.getDialog().dismiss();
            mResetChargeDialog = null;
        }
    }

    @Override
    public void show(View parent) {
        super.show(parent);
        ThreadPoolManager.getInstance().execute(new MapRunnable());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_build_map:
                if (tvBuildMap.isSelected()) {
                    tvBuildMap.setSelected(false);
                } else {
                    tvBuildMap.setSelected(true);
                }
                SlamManager.getInstance().setMapUpdateInThread(tvBuildMap.isSelected(), null);
                break;
            case R.id.tv_add_point:
                dismiss();
                if (mOnMapListener != null) {
                    mOnMapListener.onMapAddPoint();
                }
                break;
            case R.id.tv_reset_charge:
                if (mOnMapListener != null) {
                    mOnMapListener.onMapResetCharge();
                }
                break;
            case R.id.tv_relocation:
                dismiss();
                relocation();
                break;
            case R.id.tv_clean_map:
                if (mOnMapListener != null) {
                    mOnMapListener.onMapClean();
                }
                break;
            case R.id.tv_save_map:
                dismiss();
                if (mOnMapListener != null) {
                    mOnMapListener.onMapSave();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onName(String name) {
        if (TextUtils.isEmpty(name)) {
            ToastUtils.getInstance(mContext).show(mContext.getString(R.string.map_name_empty_tips));
            return;
        }
        closeNumberInputDialog();
        showDialogTips(mContext.getString(R.string.map_save_tips));
        saveMap(name);
    }

    @Override
    public void onConfirm() {
        dismiss();
        cleanMap();
    }

    public void showAddPointViewDialog(FragmentManager fragmentManager, AddPointViewDialog.OnPointListener listener) {
        if (!isAddPointViewDialogShow()) {
            mAddPointViewDialog = AddPointViewDialog.newInstance();
            mAddPointViewDialog.setOnPointListener(listener);
            mAddPointViewDialog.show(fragmentManager, "ADD_POINT_DIALOG");
        }
    }

    public void showResetChargeDialog(FragmentManager fragmentManager) {
        if (!isResetChargeDialogShow()) {
            mResetChargeDialog = ResetChargeDialog.newInstance();
            mResetChargeDialog.show(fragmentManager, "RESET_CHARGE_DIALOG");
        }
    }

    public void showNameInputDialog(FragmentManager fragmentManager) {
        if (!isNameInputDialogShow()) {
            mNameInputDialog = NameInputDialog.newInstance(mContext.getString(R.string.tv_title_save_map), mContext.getString(R.string.map_rule_tips), mContext.getString(R.string.et_hint_map_tips));
            mNameInputDialog.setOnNameListener(this);
            mNameInputDialog.show(fragmentManager, "NAME_INPUT_DIALOG");
        }
    }

    public void showLoadTipsDialog(FragmentManager fragmentManager, String tips) {
        if (!isLoadTipsDialogShow()) {
            mLoadTipsDialog = LoadTipsDialog.newInstance(tips);
            mLoadTipsDialog.show(fragmentManager, "LOAD_TIPS_DIALOG");
        }
    }

    public void showConfirmDialog(FragmentManager fragmentManager, String tips) {
        if (!isConfirmDialogShow()) {
            mConfirmDialog = ConfirmDialog.newInstance(tips);
            mConfirmDialog.setOnConfirmListener(this);
            mConfirmDialog.show(fragmentManager, "CLEAN_MAP_DIALOG");
        }
    }

    private void relocation() {
        showDialogTips(mContext.getString(R.string.relocation_map_tips));
        SlamManager.getInstance().recoverLocationByDefault(new OnResultListener<Boolean>() {
            @Override
            public void onResult(Boolean data) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoadTipsDialog();
                        ToastUtils.getInstance(mContext).show(mContext.getString(data ? R.string.relocation_map_success : R.string.relocation_map_fail));
                    }
                });
            }
        });
    }

    private void cleanMap() {
        showDialogTips(mContext.getString(R.string.map_clean_tips));
        SlamManager.getInstance().clearMapInThread(new OnResultListener<Boolean>() {
            @Override
            public void onResult(Boolean data) {
                if (data) {
                    MyDBSource.getInstance(mContext).deleteAllLocation();
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.cleanMapResult(data);
                        closeLoadTipsDialog();
                        ToastUtils.getInstance(mContext).show(mContext.getString(data ? R.string.map_clean_success_tips : R.string.map_clean_fail_tips));
                    }
                });
            }
        });
    }

    private void saveMap(String number) {
        final List<LocationBean> beanList = DataHelper.getInstance().getLocationBeanList(mContext, number);
        SlamManager.getInstance().saveMapInThread(BaseConstant.getMapDirectory(mContext), BaseConstant.getMapFileName(number), beanList, new OnResultListener<Boolean>() {
            @Override
            public void onResult(Boolean data) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoadTipsDialog();
                        ToastUtils.getInstance(mContext).show(mContext.getString(data ? R.string.map_save_success_tips : R.string.map_save_fail_tips));
                    }
                });
            }
        });
    }

    private void closeLoadTipsDialog() {
        try {
            if (isLoadTipsDialogShow()) {
                mLoadTipsDialog.getDialog().dismiss();
                mLoadTipsDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isLoadTipsDialogShow() {
        return mLoadTipsDialog != null && mLoadTipsDialog.getDialog() != null && mLoadTipsDialog.getDialog().isShowing();
    }

    private void closeAddPointViewDialog() {
        if (isAddPointViewDialogShow()) {
            mAddPointViewDialog.getDialog().dismiss();
            mAddPointViewDialog = null;
        }
    }

    private void closeNumberInputDialog() {
        if (isNameInputDialogShow()) {
            mNameInputDialog.getDialog().dismiss();
            mNameInputDialog = null;
        }
    }

    private boolean isAddPointViewDialogShow() {
        return mAddPointViewDialog != null && mAddPointViewDialog.getDialog() != null && mAddPointViewDialog.getDialog().isShowing();
    }

    private boolean isResetChargeDialogShow() {
        return mResetChargeDialog != null && mResetChargeDialog.getDialog() != null && mResetChargeDialog.getDialog().isShowing();
    }

    private boolean isNameInputDialogShow() {
        return mNameInputDialog != null && mNameInputDialog.getDialog() != null && mNameInputDialog.getDialog().isShowing();
    }

    private void showDialogTips(String tips) {
        if (mOnMapListener != null) {
            mOnMapListener.onMapShowTipsDialog(tips);
        }
    }

    private boolean isConfirmDialogShow() {
        return mConfirmDialog != null && mConfirmDialog.getDialog() != null && mConfirmDialog.getDialog().isShowing();
    }

    private class MapRunnable implements Runnable {
        @Override
        public void run() {
            boolean isUpdate = SlamManager.getInstance().isMapUpdate();
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvBuildMap.setSelected(isUpdate);
                }
            });
        }
    }

    public interface OnMapListener {
        /**
         * 建点
         */
        void onMapAddPoint();

        /**
         * 重置充电桩
         */
        void onMapResetCharge();

        /**
         * 提示dialog
         */
        void onMapShowTipsDialog(String tips);

        /**
         * 清除地图
         */
        void onMapClean();

        /**
         * 保存地图
         */
        void onMapSave();
    }
}
