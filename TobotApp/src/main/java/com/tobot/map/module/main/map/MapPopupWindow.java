package com.tobot.map.module.main.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.RectF;
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
import com.tobot.map.util.MediaScanner;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;
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
public class MapPopupWindow extends BasePopupWindow implements PopupWindow.OnDismissListener, NameInputDialog.OnNameListener, ConfirmDialog.OnConfirmListener, OnResultListener<Boolean> {
    private final MainActivity mActivity;
    private final AddPointViewDialog.OnPointListener mOnPointListener;
    private TextView tvBuildMap;
    private AddPointViewDialog mAddPointViewDialog;
    private ResetChargeDialog mResetChargeDialog;
    private NameInputDialog mNameInputDialog;
    private LoadTipsDialog mLoadTipsDialog;
    private ConfirmDialog mConfirmDialog;
    private boolean isRelocationPart;

    public MapPopupWindow(Context context, WeakReference<MainActivity> reference, AddPointViewDialog.OnPointListener listener) {
        super(context);
        mActivity = reference.get();
        mOnPointListener = listener;
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
        view.findViewById(R.id.tv_manual_add_location).setOnClickListener(this);
        view.findViewById(R.id.tv_clear_map).setOnClickListener(this);
        view.findViewById(R.id.tv_save_map).setOnClickListener(this);
    }

    @Override
    public void onDismiss() {
        closeAddPointViewDialog();
        closeNameInputDialog();
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_build_map:
                tvBuildMap.setSelected(!tvBuildMap.isSelected());
                SlamManager.getInstance().setMapUpdateAsync(tvBuildMap.isSelected(), null);
                break;
            case R.id.tv_add_point:
                dismiss();
                showAddPointViewDialog();
                break;
            case R.id.tv_reset_charge:
                dismiss();
                showResetChargeDialog();
                break;
            case R.id.tv_relocation:
                dismiss();
                isRelocationPart = DataHelper.getInstance().getRelocationType(mContext) == SlamCode.RELOCATION_PART;
                if (isRelocationPart) {
                    if (mActivity != null) {
                        mActivity.setRelocationPart(true);
                    }
                    return;
                }

                relocateGlobal();
                break;
            case R.id.tv_manual_add_location:
                dismiss();
                mActivity.manualAddLocation();
                break;
            case R.id.tv_clear_map:
                dismiss();
                showConfirmDialog(mContext.getString(R.string.tv_clear_map_tips));
                break;
            case R.id.tv_save_map:
                dismiss();
                showNameInputDialog();
                break;
            default:
                break;
        }
    }

    @Override
    public void onName(String name) {
        if (TextUtils.isEmpty(name)) {
            ToastUtils.getInstance(mContext).show(R.string.map_name_empty_tips);
            return;
        }

        closeNameInputDialog();
        showLoadTipsDialog(mContext.getString(R.string.save_ing));
        saveMap(name);
    }

    @Override
    public void onConfirm(boolean isConfirm) {
        if (isConfirm) {
            clearMap();
        }
    }

    @Override
    public void onResult(Boolean data) {
        // 重定位结果
        String content = "";
        if (!data) {
            StringBuilder builder = new StringBuilder();
            if (SlamManager.getInstance().isSystemBrakeStop()) {
                builder.append(mContext.getString(R.string.break_stop_tips));
            }

            if (SlamManager.getInstance().isSystemEmergencyStop()) {
                builder.append(mContext.getString(R.string.emergency_stop_tips));
            }
            content = builder.toString().trim();
        }

        final String tips = content;
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeLoadTipsDialog();
                if (!data && !TextUtils.isEmpty(tips)) {
                    ToastUtils.getInstance(mContext).show(tips);
                } else {
                    ToastUtils.getInstance(mContext).show(data ? R.string.relocate_success : R.string.relocate_fail);
                }

                if (isRelocationPart) {
                    mActivity.setRelocationPart(false);
                }
            }
        });
    }

    private void showAddPointViewDialog() {
        if (!isAddPointViewDialogShow()) {
            mAddPointViewDialog = AddPointViewDialog.newInstance();
            mAddPointViewDialog.setOnPointListener(mOnPointListener);
            mAddPointViewDialog.show(mActivity.getSupportFragmentManager(), "ADD_POINT_DIALOG");
        }
    }

    private void showResetChargeDialog() {
        if (!isResetChargeDialogShow()) {
            mResetChargeDialog = ResetChargeDialog.newInstance();
            mResetChargeDialog.show(mActivity.getSupportFragmentManager(), "RESET_CHARGE_DIALOG");
        }
    }

    private void showNameInputDialog() {
        if (!isNameInputDialogShow()) {
            mNameInputDialog = NameInputDialog.newInstance(mContext.getString(R.string.tv_title_save_map), mContext.getString(R.string.map_rule_tips), mContext.getString(R.string.et_hint_map_tips));
            mNameInputDialog.setOnNameListener(this);
            mNameInputDialog.show(mActivity.getSupportFragmentManager(), "NAME_INPUT_DIALOG");
        }
    }

    private void showLoadTipsDialog(String tips) {
        if (!isLoadTipsDialogShow()) {
            mLoadTipsDialog = LoadTipsDialog.newInstance(tips);
            mLoadTipsDialog.show(mActivity.getSupportFragmentManager(), "LOAD_TIPS_DIALOG");
        }
    }

    private void showConfirmDialog(String tips) {
        if (!isConfirmDialogShow()) {
            mConfirmDialog = ConfirmDialog.newInstance(tips);
            mConfirmDialog.setOnConfirmListener(this);
            mConfirmDialog.show(mActivity.getSupportFragmentManager(), "CLEAR_MAP_DIALOG");
        }
    }

    public void relocatePart(RectF area) {
        String width = area != null ? mContext.getString(R.string.float_1_format, Math.abs(area.width())) : "0";
        String height = area != null ? mContext.getString(R.string.float_1_format, Math.abs(area.height())) : "0";
        String tips = width + " x " + height;
        showLoadTipsDialog(mContext.getString(R.string.relocate_ing_with_size, tips));
        SlamManager.getInstance().recoverLocationByCustom(area, true, this);
    }

    private void relocateGlobal() {
        showLoadTipsDialog(mContext.getString(R.string.relocate_ing));
        SlamManager.getInstance().recoverLocationByDefault(this);
    }

    private void clearMap() {
        showLoadTipsDialog(mContext.getString(R.string.clear_ing));
        SlamManager.getInstance().clearMapAsync(new OnResultListener<Boolean>() {
            @Override
            public void onResult(Boolean data) {
                if (data) {
                    MyDBSource.getInstance(mContext).deleteAllLocation();
                    DataHelper.getInstance().setCurrentMapFile("");
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.clearMapResult(data);
                        closeLoadTipsDialog();
                        ToastUtils.getInstance(mContext).show(data ? R.string.clear_success : R.string.clear_fail);
                    }
                });
            }
        });
    }

    private void saveMap(String name) {
        final List<LocationBean> beanList = DataHelper.getInstance().getLocationBeanList(mContext, name);
        String mapFile = BaseConstant.getMapFile(name);
        SlamManager.getInstance().saveMapAsync(BaseConstant.getMapDirectory(mContext), mapFile, beanList, new OnResultListener<Boolean>() {
            @Override
            public void onResult(Boolean data) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeLoadTipsDialog();
                        if (data) {
                            DataHelper.getInstance().setCurrentMapFile(mapFile);
                            new MediaScanner().scanFile(mContext, BaseConstant.getMapFilePath(mContext, mapFile));
                        }
                        ToastUtils.getInstance(mContext).show(data ? R.string.save_success : R.string.save_fail);
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

    private void closeNameInputDialog() {
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
}
