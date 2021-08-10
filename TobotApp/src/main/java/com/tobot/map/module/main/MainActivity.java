package com.tobot.map.module.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slamtec.slamware.action.ActionStatus;
import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.robot.Pose;
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
import com.tobot.map.module.main.warning.WarningInfo;
import com.tobot.map.module.set.SetActivity;
import com.tobot.map.module.task.Task;
import com.tobot.map.module.task.TaskActivity;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.SlamCode;
import com.tobot.slam.agent.listener.OnSlamExceptionListener;
import com.tobot.slam.data.LocationBean;
import com.tobot.slam.data.Rubber;
import com.tobot.slam.view.MapView;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2019/10/18
 */
public class MainActivity extends BaseActivity implements MapView.OnMapListener, AddPointViewDialog.OnPointListener, MapPopupWindow.OnMapListener, OnEditListener,
        ActionPopupWindow.OnChargeListener, OnSlamExceptionListener, OnDialogBackEventListener, TipsDialog.OnConfirmListener {
    @BindView(R.id.map_view)
    MapView mapView;
    @BindView(R.id.tv_status)
    TextView tvStatus;
    @BindView(R.id.tv_map)
    TextView tvMap;
    @BindView(R.id.tv_action)
    TextView tvAction;
    @BindView(R.id.tv_edit)
    TextView tvEdit;
    @BindView(R.id.tv_pose_show)
    TextView tvPoseShow;
    @BindView(R.id.tv_rssi)
    TextView tvRssi;
    @BindView(R.id.tv_count)
    TextView tvCount;
    @BindView(R.id.tv_socket_connect_success_tips)
    TextView tvSocketConnectTips;
    @BindView(R.id.ll_control)
    LinearLayout llControl;
    @BindView(R.id.iv_set)
    ImageView ivSet;
    @BindView(R.id.tv_navigate)
    TextView tvNavigate;
    @BindView(R.id.view_edit_line)
    EditLineView editLineView;
    @BindView(R.id.view_rubber_edit)
    RubberEditView rubberEditView;
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
    private int mEditType, mOption, mLowBatteryStatus, mLocationQuality;
    private MapClickHandle mMapClickHandle;
    private boolean isHandleMove, isShowTips, isUpdateMap, isDisconnect;
    private Charge mCharge;
    private static final int LOW_BATTERY = 1;
    private TipsDialog mTipsDialog;
    private SensorWarningDialog mSensorWarningDialog;

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
        mapView.setOnMapListener(this);
        mMapHelper = new MapHelper(new WeakReference<>(this), new WeakReference<>(this), new WeakReference<>(mapView));
        mMapClickHandle = new MapClickHandle(new WeakReference<>(this), new WeakReference<>(mapView));
        mNavigate = new Navigate(new WeakReference<>(this), new WeakReference<>(this));
        mTask = new Task(new WeakReference<>(this), new WeakReference<>(this));
        mCharge = new Charge(new WeakReference<>(this), new WeakReference<>(this));
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
                if (mMapHelper != null) {
                    mMapHelper.updateMap();
                }
                return;
            }

            // 执行任务
            if (requestCode == CODE_TASK && data != null) {
                List<LocationBean> locationBeanList = data.getParcelableArrayListExtra(BaseConstant.DATA_KEY);
                boolean isAddCharge = data.getBooleanExtra(BaseConstant.CONTENT_KEY, false);
                int loopCount = data.getIntExtra(BaseConstant.LOOP_KEY, 0);
                DataHelper.getInstance().clearWarningList();
                if (mTask != null) {
                    mTask.execute(locationBeanList, isAddCharge, loopCount);
                }
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
        if (isUpdateMap && mMapHelper != null) {
            isUpdateMap = false;
            mMapHelper.startUpdateMap();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeLoadTipsDialog();
        if (isTipsDialogShow()) {
            mTipsDialog.getDialog().dismiss();
            mTipsDialog = null;
        }

        isUpdateMap = true;
        if (mMapHelper != null) {
            mMapHelper.stopUpdateMap();
        }

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
    public void onMapClick(MotionEvent event) {
        if (mMapClickHandle != null) {
            mMapClickHandle.handleMapClick(mEditType, mOption, event, isHandleMove);
        }
    }

    @Override
    public void onSensorStatus(SensorType sensorType, int id, boolean isTrigger) {
        if (isTrigger && sensorType != null) {
            Logger.i(BaseConstant.TAG, "sensorType=" + sensorType.toString() + ",id=" + id);
            // 记录警告信息
            recordWarningInfo(id, sensorType.toString());
            showTipsDialog(DataHelper.getInstance().getSensorTips(this, sensorType, id));
        }
    }

    @Override
    public void onHealthInfo(int code, String info) {
        recordWarningInfo(code, info);
        showTipsDialog(info);
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
    public void onMapAddPoint() {
        if (mMapPopupWindow != null) {
            mMapPopupWindow.showAddPointViewDialog(getSupportFragmentManager(), this);
        }
    }

    @Override
    public void onMapResetCharge() {
        if (mMapPopupWindow != null) {
            mMapPopupWindow.showResetChargeDialog(getSupportFragmentManager());
        }
    }

    @Override
    public void onMapShowTipsDialog(String tips) {
        if (mMapPopupWindow != null) {
            mMapPopupWindow.showLoadTipsDialog(getSupportFragmentManager(), tips);
        }
    }

    @Override
    public void onMapClean() {
        if (mMapPopupWindow != null) {
            mMapPopupWindow.showConfirmDialog(getSupportFragmentManager(), getString(R.string.tv_clean_map_tips));
        }
    }

    @Override
    public void onMapSave() {
        if (mMapPopupWindow != null) {
            mMapPopupWindow.showNameInputDialog(getSupportFragmentManager());
        }
    }

    @Override
    public void onEditClick(int type) {
        mEditType = type;
        llControl.setVisibility(View.GONE);
        ivSet.setVisibility(View.GONE);
        if (type == OnEditListener.TYPE_RUBBER) {
            rubberEditView.init(this);
            return;
        }

        editLineView.init(type, addLineView, this);
    }

    @Override
    public void onEditOption(int option) {
        mOption = option;
        switch (option) {
            case OnEditListener.OPTION_CLOSE:
                if (editLineView.getVisibility() == View.VISIBLE) {
                    removeEditLineView();
                    return;
                }

                removeRubberView();
                break;
            case OnEditListener.OPTION_WIPE_WHITE:
                mapView.setRubberMode(Rubber.RUBBER_WHITE);
                break;
            case OnEditListener.OPTION_WIPE_GREY:
                mapView.setRubberMode(Rubber.RUBBER_GREY);
                break;
            case OnEditListener.OPTION_WIPE_BLACK:
                mapView.setRubberMode(Rubber.RUBBER_BLACK);
                break;
            case OnEditListener.OPTION_WIPE_CANCEL:
                mapView.closeRubber();
                break;
            case OnEditListener.OPTION_CLEAR:
                if (mMapClickHandle != null) {
                    mMapClickHandle.clearLines(mEditType);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onAddLine(PointF pointF) {
        if (mMapClickHandle != null) {
            mMapClickHandle.addLine(mEditType, pointF);
        }
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
        if (!TextUtils.isEmpty(error)) {
            String operationError = "Operation Failed";
            if (error.contains(operationError)) {
//                handleOperationFailed();
                return;
            }

            String[] connectionFailArray = {"Connection Failed", "Connection Lost"};
            if (error.contains(connectionFailArray[0]) || error.contains(connectionFailArray[1])) {
                handleConnectionFailed(error);
            }
        }
    }

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

    private void handleConnectionFailed(String error) {
        if (isFinish) {
            return;
        }

        if (!isDisconnect) {
            isDisconnect = true;
            showTipsDialog(getString(R.string.slam_disconnect_tips, error));
        }
    }

    private void handleOperationFailed() {
        if (!isFinish) {
            showToastTips(getString(R.string.operate_error_tips));
        }
    }

    private void recordWarningInfo(int id, String content) {
        WarningInfo info = WarningInfo.getWarningInfo();
        info.setId(id);
        info.setType(content);
        info.setCount(1);
        DataHelper.getInstance().setWarningData(info);
    }

    public void moveTo(float x, float y, float yaw) {
        if (mNavigate != null) {
            mNavigate.moveTo(x, y, yaw);
        }
    }

    public void showToast(String tips) {
        showToastTips(tips);
    }

    public void updatePoseShow(Pose pose) {
        if (pose != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvPoseShow.setText(getString(R.string.tv_pose_show, pose.getX(), pose.getY(), (float) (pose.getYaw() * 180 / Math.PI)));
                }
            });
        }
    }

    public void updateStatus(int battery, boolean isCharge, int locationQuality, ActionStatus actionStatus) {
        mLocationQuality = locationQuality;
        String chargeStatus = isCharge ? getString(R.string.tv_charge_true) : getString(R.string.tv_charge_false);
        String status = actionStatus != null ? actionStatus.toString() : getString(R.string.unknown);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvStatus.setText(getString(R.string.tv_status_show, locationQuality, chargeStatus, battery, status));
                handleLowBattery(battery, isCharge);
            }
        });
    }

    public void cleanMapResult(boolean isSuccess) {
        if (isSuccess) {
            mapView.clearLocationLabel();
            mapView.setCentred();
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

    public void setTaskCount(String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCount.setText(content);
            }
        });
    }

    public void handleMoveResult(boolean isSuccess) {
        if (!isSuccess) {
            // 建图时定位质量为0
            if (mLocationQuality > 0 && mLocationQuality < SlamCode.RECOVER_QUALITY_MIN) {
                showTipsDialog(getString(R.string.move_recover_quality_low_tips, SlamCode.RECOVER_QUALITY_MIN));
            }
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
                onCharge();
            }
            return;
        }

        mLowBatteryStatus = 0;
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
        if (mTask != null) {
            mTask.stop();
        }

        if (mNavigate != null) {
            mNavigate.stop();
        }

        if (mCharge != null) {
            mCharge.stop();
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
        mOption = OnEditListener.OPTION_CLOSE;
        editLineView.remove();
        llControl.setVisibility(View.VISIBLE);
        ivSet.setVisibility(View.VISIBLE);
    }

    private void removeRubberView() {
        mOption = OnEditListener.OPTION_CLOSE;
        rubberEditView.remove();
        mapView.closeRubber();
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

    public void showTipsDialog(String tips) {
        if (TextUtils.isEmpty(tips)) {
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

    private boolean isTipsDialogShow() {
        return mTipsDialog != null && mTipsDialog.getDialog() != null && mTipsDialog.getDialog().isShowing();
    }

    private boolean isSensorWarningDialogShow() {
        return mSensorWarningDialog != null && mSensorWarningDialog.getDialog() != null && mSensorWarningDialog.getDialog().isShowing();
    }
}
