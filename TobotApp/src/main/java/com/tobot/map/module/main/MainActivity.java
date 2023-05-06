package com.tobot.map.module.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.robot.SensorType;
import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.base.OnDialogBackEventListener;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.common.TipsDialog;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.action.ActionPopupWindow;
import com.tobot.map.module.main.action.Charge;
import com.tobot.map.module.main.edit.AddLineView;
import com.tobot.map.module.main.edit.EditLineView;
import com.tobot.map.module.main.edit.EditPopupWindow;
import com.tobot.map.module.main.edit.OnEditListener;
import com.tobot.map.module.main.edit.RubberEditView;
import com.tobot.map.module.main.map.AddPointViewDialog;
import com.tobot.map.module.main.map.MapPopupWindow;
import com.tobot.map.module.main.map.Navigate;
import com.tobot.map.module.main.warning.SensorWarningDialog;
import com.tobot.map.module.set.SetActivity;
import com.tobot.map.module.task.Task;
import com.tobot.map.module.task.TaskActivity;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;
import com.tobot.slam.agent.listener.OnActionListener;
import com.tobot.slam.agent.listener.OnSlamExceptionListener;
import com.tobot.slam.data.LocationBean;
import com.tobot.slam.view.MapView;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2019/10/18
 */
public class MainActivity extends BaseActivity implements MapView.OnMapListener, AddPointViewDialog.OnPointListener, OnEditListener, ActionPopupWindow.OnChargeListener, OnSlamExceptionListener, OnActionListener, OnDialogBackEventListener, TipsDialog.OnConfirmListener, ReconnectSlamThread.OnReconnectResultListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.map_view)
    MapView mapView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_status)
    TextView tvStatus;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_map)
    TextView tvMap;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_action)
    TextView tvAction;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_edit)
    TextView tvEdit;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_pose_show)
    TextView tvPoseShow;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_rssi)
    TextView tvRssi;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_count)
    TextView tvCount;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_control)
    LinearLayout llControl;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.iv_set)
    ImageView ivSet;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_navigate)
    TextView tvNavigate;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_edit_line)
    EditLineView editLineView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_rubber_edit)
    RubberEditView rubberEditView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_add_line)
    AddLineView addLineView;
    private static final int CODE_SET = 1;
    private static final int CODE_TASK = 2;
    private MapHelper mMapHelper;
    private Navigate mNavigate;
    private Task mTask;
    private ActionPopupWindow mActionPopupWindow;
    private MapPopupWindow mMapPopupWindow;
    private EditPopupWindow mEditPopupWindow;
    private int mLowBatteryStatus, mLocationQuality, mLostCount;
    private boolean isHandleMove, isShowTips, isUpdateMap, isDisconnect, isShowRelocationTips, isLowBatteryTips;
    private Charge mCharge;
    private static final int LOW_BATTERY = 1;
    private TipsDialog mTipsDialog;
    private SensorWarningDialog mSensorWarningDialog;
    private List<LocationBean> mLocationList;
    private long mLostTime;
    private String mLostError;
    private ReconnectSlamThread mReconnectSlamThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init() {
        SlamManager.getInstance().setOnSlamExceptionListener(this);
        SlamManager.getInstance().setOnActionListener(this);
        mapView.setOnMapListener(this);
        mapView.setTouchMoveCount(DataHelper.getInstance().getTouchCount(this));
        mMapHelper = new MapHelper(new WeakReference<>(this), new WeakReference<>(this), new WeakReference<>(mapView));
        mNavigate = new Navigate(new WeakReference<>(this), new WeakReference<>(this));
        mCharge = new Charge(new WeakReference<>(this), new WeakReference<>(this));
        // 如果有位置点的话提示
        mLocationList = MyDBSource.getInstance(this).queryLocationList();
        if (mLocationList != null && !mLocationList.isEmpty()) {
            showConfirmDialog(getString(R.string.tv_location_load_tips));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.i(BaseConstant.TAG, "MainActivity requestCode=" + requestCode + ",resultCode=" + resultCode);
        if (resultCode == BaseConstant.CODE_EXIT) {
            finish();
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            // 切换地图
            if (requestCode == CODE_SET) {
                // 地图界面上的位置点提示
                mapView.addLocationLabel(true, MyDBSource.getInstance(this).queryLocationList());
                // 切换后要更新一下地图界面
                updateMap();
                return;
            }

            // 执行任务
            if (requestCode == CODE_TASK && data != null) {
                List<LocationBean> locationBeanList = data.getParcelableArrayListExtra(BaseConstant.DATA_KEY);
                boolean isAddCharge = data.getBooleanExtra(BaseConstant.CONTENT_KEY, false);
                int loopCount = data.getIntExtra(BaseConstant.LOOP_KEY, 0);
//                DataHelper.getInstance().clearWarningList();
                if (mTask == null) {
                    mTask = new Task(new WeakReference<>(this), new WeakReference<>(this));
                }
                mTask.execute(locationBeanList, isAddCharge, loopCount);
            }
        }
    }

    @Override
    public void onBackPressed() {
        // 如果当前是编辑墙的话，不处理返回键
        if (editLineView.getVisibility() == View.VISIBLE) {
            removeEditLineView();
            return;
        }

        if (rubberEditView.getVisibility() == View.VISIBLE) {
            removeRubberView();
            return;
        }

        if (isClosePopupWindow()) {
            return;
        }

        super.onBackPressed();
    }

    @Override
    public void onDialogBackEvent() {
        if (isShowTips) {
            isShowTips = false;
            handleTvStopClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 切换过界面才会再去刷新地图
        if (isUpdateMap && mMapHelper != null) {
            isUpdateMap = false;
            mMapHelper.startUpdateMap();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeReconnectSlamThread();
        closeLoadTipsDialog();
        closeConfirmDialog();
        if (isTipsDialogShow()) {
            mTipsDialog.getDialog().dismiss();
            mTipsDialog = null;
        }

        isUpdateMap = true;
        if (mMapHelper != null) {
            mMapHelper.stopUpdateMap();
        }

        // 记录传感器信息
        DataHelper.getInstance().recordWarningList(this);
        if (isSensorWarningDialogShow()) {
            mSensorWarningDialog.getDialog().dismiss();
            mSensorWarningDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMapHelper != null) {
            mMapHelper.destroy();
            mMapHelper = null;
        }

        isClosePopupWindow();
        stopService(new Intent(getApplicationContext(), MapService.class));
    }

    @Override
    public void onConfirm(boolean isConfirm) {
        if (isConfirm) {
            mapView.addLocationLabel(true, mLocationList);
            return;
        }

        MyDBSource.getInstance(this).deleteAllLocation();
    }

    @Override
    public void onMapClick(MotionEvent event) {
        if (isHandleMove) {
            PointF pointF = mapView.widgetCoordinateToMapCoordinate(event.getX(), event.getY());
            mapView.setClickTips(pointF);
            if (pointF != null) {
                LocationBean bean = new LocationBean();
                bean.setX(pointF.getX());
                bean.setY(pointF.getY());
                onMoveTo(bean);
            }
        }
    }

    @Override
    public void onSensorStatus(SensorType sensorType, int id, boolean isTrigger) {
        if (isTrigger && sensorType != null) {
            // 记录警告信息
            DataHelper.getInstance().setWarningData(id, sensorType.toString());
            showTipsDialog(DataHelper.getInstance().getSensorTips(this, sensorType, id));
        }
    }

    @Override
    public void onHealthInfo(int code, String info) {
        // 如果电机异常的话，就停止运动
        if (code == SlamCode.CODE_MOTOR_ERROR) {
            handleTvStopClick();
        }

        DataHelper.getInstance().setWarningData(code, info);
        DataHelper.getInstance().recordImportantInfo(this, info);
//        showTipsDialog(info);
    }

    @Override
    public void onRelocationArea(RectF area) {
        Logger.i(BaseConstant.TAG, "onRelocationArea()");
        if (mMapPopupWindow != null) {
            mMapPopupWindow.relocationPart(area);
        }
    }

    @Override
    public void onMoveTo(LocationBean data) {
        if (mNavigate != null) {
            mNavigate.moveTo(data);
        }
    }

    @Override
    public void onAddLocationLabel(LocationBean data) {
        mapView.addLocationLabel(data);
    }

    @Override
    public void onUpdateLocationLabel(String oldNumber, LocationBean data) {
        mapView.updateLocationLabel(oldNumber, data);
    }

    @Override
    public void onDeleteLocationLabel(String number) {
        mapView.deleteLocationLabel(number);
    }

    @Override
    public void onEditClick(int type) {
        llControl.setVisibility(View.GONE);
        ivSet.setVisibility(View.GONE);
        if (type == OnEditListener.TYPE_RUBBER) {
            rubberEditView.init(mapView, this);
            return;
        }

        editLineView.init(type, mapView, addLineView, this);
    }

    @Override
    public void onEditClose() {
        if (editLineView.getVisibility() == View.VISIBLE) {
            removeEditLineView();
            return;
        }

        removeRubberView();
    }

    @Override
    public void onCharge() {
        if (mCharge != null) {
            mCharge.goCharge();
        }
    }

    @Override
    public void onConfirm() {
        if (isDisconnect) {
            finish();
        }
    }

    @Override
    public void onSlamException(Exception e) {
        String error = e.getMessage();
        Logger.i(BaseConstant.TAG, "slam error=" + error);
        if (TextUtils.isEmpty(error)) {
            return;
        }

//        if (error.contains("Operation Failed")) {
//            handleOperationFailed();
//            return;
//        }

        if (DataHelper.getInstance().isSlamError(error)) {
            mLostError = error;
            mLostCount++;
            // 重连，在特定时间内连续断连一定次数后再去重连
            long faultTime = 20000;
            mLostTime = mLostTime == 0 ? System.currentTimeMillis() : mLostTime;
            if (System.currentTimeMillis() - mLostTime <= faultTime) {
                int maxCount = 15;
                if (mLostCount > maxCount) {
                    mLostCount = 0;
                    if (mReconnectSlamThread == null) {
                        mReconnectSlamThread = new ReconnectSlamThread(this);
                        mReconnectSlamThread.start();
                        DataHelper.getInstance().recordImportantInfo(this, "slam reconnect");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isFinish) {
                                    showLoadTipsDialog(getString(R.string.tv_reconnect_ing), null);
                                }
                            }
                        });
                    }
                }
                return;
            }

            mLostCount = 0;
            if (mReconnectSlamThread == null) {
                mLostTime = 0;
            }
        }
    }

    @Override
    public void onActionReason(String actionName, String reason) {
        DataHelper.getInstance().recordImportantInfo(this, actionName + ":" + reason);
    }

    @Override
    public void onReconnectResult(boolean isSuccess) {
        closeReconnectSlamThread();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeLoadTipsDialog();
            }
        });

        mLostCount = 0;
        mLostTime = 0;
        if (isSuccess) {
            DataHelper.getInstance().recordImportantInfo(this, "slam reconnect success");
            showToastTips(getString(R.string.reconnect_success));
            return;
        }

        if (!isDisconnect) {
            isDisconnect = true;
            DataHelper.getInstance().recordImportantInfo(this, "slam reconnect fail error=" + mLostError);
            showTipsDialog(getString(R.string.slam_disconnect_tips, mLostError));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.tv_map, R.id.tv_action, R.id.tv_edit, R.id.tv_stop, R.id.tv_task, R.id.tv_navigate, R.id.iv_set, R.id.iv_warning})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.tv_map:
                handleTvMapClick();
                break;
            case R.id.tv_action:
                handleTvActionClick();
                break;
            case R.id.tv_edit:
                handleTvEditClick();
                break;
            case R.id.tv_stop:
                handleTvStopClick();
                break;
            case R.id.tv_task:
                startActivityForResult(new Intent(this, TaskActivity.class), CODE_TASK);
                break;
            case R.id.tv_navigate:
                handleTvNavigateClick();
                break;
            case R.id.iv_set:
                startActivityForResult(new Intent(this, SetActivity.class), CODE_SET);
                break;
            case R.id.iv_warning:
                if (!isSensorWarningDialogShow()) {
                    mSensorWarningDialog = SensorWarningDialog.newInstance(DataHelper.getInstance().getWarningList());
                    mSensorWarningDialog.show(getSupportFragmentManager(), "SENSOR_WARNING_DIALOG");
                }
                break;
            default:
                break;
        }
    }

    private void handleOperationFailed() {
        if (!isFinish) {
            showToastTips(getString(R.string.operate_error_tips));
        }
    }

    public void showToast(String tips) {
        if (!isFinish) {
            showToastTips(tips);
        }
    }

    public void updatePoseShow(float x, float y, float yaw) {
        if (!isFinish) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvPoseShow.setText(getString(R.string.tv_pose_show, String.valueOf(x), String.valueOf(y), String.valueOf(yaw)));
                }
            });
        }
    }

    public void updateStatus(int battery, boolean isCharge, String chargeMode, int locationQuality, String status) {
        if (isFinish) {
            return;
        }

        mLocationQuality = locationQuality;
        String chargeStatus = getString(isCharge ? R.string.tv_charge_true : R.string.tv_charge_false);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText(getString(R.string.tv_status_show, locationQuality, chargeStatus, chargeMode, battery, status));
                handleLowBattery(battery, isCharge);
            }
        });
    }

    public void clearMapResult(boolean isSuccess) {
        if (isSuccess) {
            mapView.clearMap();
            updateMap();
        }
    }

    public void setRssi(int rssiId, String tips) {
        if (isFinish) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvRssi.setText(getString(R.string.tv_signal_tips, rssiId, tips));
            }
        });
    }

    public void showTipsDialog(String tips) {
        if (isFinish || TextUtils.isEmpty(tips)) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isTipsDialogShow()) {
                    mTipsDialog.setContent(tips);
                    return;
                }

                mTipsDialog = TipsDialog.newInstance(tips);
                mTipsDialog.setOnConfirmListener(MainActivity.this);
                mTipsDialog.show(getSupportFragmentManager(), "TIPS_DIALOG");
            }
        });
    }

    public void setTaskCount(String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCount.setText(content);
            }
        });
    }

    public void setRelocationPart(boolean isRelocationPart) {
        mapView.setRelocationPart(isRelocationPart);
        // 只提示1次
        if (isRelocationPart && !isShowRelocationTips) {
            isShowRelocationTips = true;
            showTipsDialog(getString(R.string.tv_relocation_part_tips));
        }
    }

    public void showRelocateTips() {
        if (isFinish) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoadTipsDialog(getString(R.string.relocation_map_tips), null);
                DataHelper.getInstance().recordImportantInfo(MainActivity.this, "relocate ing");
            }
        });
    }

    public void handleRelocateResult(boolean isRelocateSuccess) {
        if (isFinish) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeLoadTipsDialog();
                showToastTips(getString(isRelocateSuccess ? R.string.relocation_map_success : R.string.relocation_map_fail));
                DataHelper.getInstance().recordImportantInfo(MainActivity.this, "relocate " + (isRelocateSuccess ? "success" : "fail"));
            }
        });
    }

    public void handleMoveFail() {
        // 建图时定位质量为0
        if (mLocationQuality > 0 && !isFinish) {
            DataHelper.getInstance().recordImportantInfo(this, "current locationQuality=" + mLocationQuality);
            int minQuality = SlamManager.getInstance().getRelocationQualityMin();
            if (mLocationQuality < minQuality) {
                showTipsDialog(getString(R.string.move_recover_quality_low_tips, minQuality));
            }
        }
    }

    public void updateMap() {
        if (mMapHelper != null) {
            mMapHelper.updateMap();
        }
    }

    private void handleLowBattery(int battery, boolean isCharge) {
        // 电量不可能为0
        if (battery <= 0) {
            return;
        }

        if (battery <= DataHelper.getInstance().getLowBattery()) {
            if (!isCharge && mLowBatteryStatus != LOW_BATTERY) {
                mLowBatteryStatus = LOW_BATTERY;
                handleTvStopClick();
                DataHelper.getInstance().recordImportantInfo(this, "low battery=" + battery);
                onCharge();
            }
            return;
        }

        mLowBatteryStatus = 0;
        if (battery <= BaseConstant.BATTERY_LOW) {
            if (!isCharge && !isLowBatteryTips) {
                isLowBatteryTips = true;
                showTipsDialog(getString(R.string.low_battery_tips, battery));
            }
            return;
        }

        isLowBatteryTips = false;
    }

    private void handleTvMapClick() {
        if (mMapPopupWindow == null) {
            mMapPopupWindow = new MapPopupWindow(this, new WeakReference<>(this), this);
        }

        if (mMapPopupWindow.isShowing()) {
            mMapPopupWindow.dismiss();
            return;
        }

        mMapPopupWindow.show(tvMap);
    }

    private void handleTvActionClick() {
        if (mActionPopupWindow == null) {
            mActionPopupWindow = new ActionPopupWindow(this, this);
        }

        if (mActionPopupWindow.isShowing()) {
            mActionPopupWindow.dismiss();
            return;
        }

        mActionPopupWindow.show(tvAction);
    }

    private void handleTvEditClick() {
        if (mEditPopupWindow == null) {
            mEditPopupWindow = new EditPopupWindow(this, this);
        }

        if (mEditPopupWindow.isShowing()) {
            mEditPopupWindow.dismiss();
            return;
        }

        mEditPopupWindow.show(tvEdit);
    }

    private void handleTvStopClick() {
        setRelocationPart(false);
        if (mTask != null) {
            mTask.stop();
            mTask = null;
            setTaskCount("");
        }

        if (mMapHelper != null) {
            mMapHelper.cancelAction();
        }
    }

    private void handleTvNavigateClick() {
        // 只有选中了导航才可以点击屏幕移动，避免误操作
        if (tvNavigate.isSelected()) {
            tvNavigate.setSelected(false);
            isHandleMove = false;
            return;
        }

        tvNavigate.setSelected(true);
        isHandleMove = true;
    }

    private void removeEditLineView() {
        editLineView.remove();
        llControl.setVisibility(View.VISIBLE);
        ivSet.setVisibility(View.VISIBLE);
    }

    private void removeRubberView() {
        rubberEditView.remove();
        llControl.setVisibility(View.VISIBLE);
        ivSet.setVisibility(View.VISIBLE);
    }

    private boolean isClosePopupWindow() {
        boolean isFlag = false;
        if (mMapPopupWindow != null && mMapPopupWindow.isShowing()) {
            mMapPopupWindow.dismiss();
            isFlag = true;
        }

        if (mActionPopupWindow != null && mActionPopupWindow.isShowing()) {
            mActionPopupWindow.dismiss();
            isFlag = true;
        }

        if (mEditPopupWindow != null && mEditPopupWindow.isShowing()) {
            mEditPopupWindow.dismiss();
            isFlag = true;
        }

        return isFlag;
    }

    private boolean isTipsDialogShow() {
        return mTipsDialog != null && mTipsDialog.getDialog() != null && mTipsDialog.getDialog().isShowing();
    }

    private boolean isSensorWarningDialogShow() {
        return mSensorWarningDialog != null && mSensorWarningDialog.getDialog() != null && mSensorWarningDialog.getDialog().isShowing();
    }

    private void closeReconnectSlamThread() {
        if (mReconnectSlamThread != null) {
            mReconnectSlamThread.close();
            mReconnectSlamThread = null;
        }
    }
}
