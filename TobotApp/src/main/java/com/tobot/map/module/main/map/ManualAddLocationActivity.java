package com.tobot.map.module.main.map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.slamtec.slamware.action.MoveDirection;
import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.slamtec.slamware.robot.Pose;
import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.log.Logger;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.DragTouchListener;
import com.tobot.slam.data.LocationBean;
import com.tobot.slam.view.MapView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2023/09/21
 */
public class ManualAddLocationActivity extends BaseActivity implements DragTouchListener.OnTouchPointListener, AddPointViewDialog.OnPointListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.map_view)
    MapView mapView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_pose)
    TextView tvPose;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_distance)
    TextView tvDistance;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_point1)
    TextView tvPoint1;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_point2)
    TextView tvPoint2;
    private static final int MSG_UPDATE = 1;
    private static final long TIME_DELAY = 1000;
    private HandlerThread mHandlerThread;
    private ThreadHandler mHandler;
    private PointF mPointAdd, mPointReference;
    private AddPointViewDialog mAddPointViewDialog;
    private Pose mCurrentPose, mManualPose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_manual_add_location;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void init() {
        mapView.setDragLinePointShow(false, true);
        new MapHelper().start();
        mHandlerThread = new HandlerThread("MAP_THREAD");
        mHandlerThread.start();
        mHandler = new ThreadHandler(new WeakReference<>(this), mHandlerThread.getLooper());
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        Logger.i(BaseConstant.TAG, "screenWidth=" + screenWidth + ",screenHeight=" + screenHeight);
        DragTouchListener dragTouchListener = new DragTouchListener(screenWidth, screenHeight, this);
        tvPoint1.setOnTouchListener(dragTouchListener);
        tvPoint2.setOnTouchListener(dragTouchListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeAddPointViewDialog();
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onTouchPoint(View view, float x, float y) {
        boolean isHandle = false;
        switch (view.getId()) {
            case R.id.tv_point1:
                isHandle = true;
                tvPoint1.setText("");
                mPointAdd = new PointF(x, y);
                Pose pose = mCurrentPose;
                if (pose != null) {
                    PointF pointF = mapView.widgetCoordinateToMapCoordinate(x, y);
                    pose.setX(pointF.getX());
                    pose.setY(pointF.getY());
                    mManualPose = pose;
                    mapView.setRobotPose(pose);
                    updatePoseShow(pose.getX(), pose.getY(), pose.getYaw());
                }
                break;
            case R.id.tv_point2:
                isHandle = true;
                tvPoint2.setText("");
                mPointReference = new PointF(x, y);
                break;
            default:
                break;
        }

        if (isHandle) {
            mapView.setDragLine(mPointAdd, mPointReference);
            if (mPointAdd != null && mPointReference != null) {
                PointF point1 = mapView.widgetCoordinateToMapCoordinate(mPointAdd.getX(), mPointAdd.getY());
                PointF point2 = mapView.widgetCoordinateToMapCoordinate(mPointReference.getX(), mPointReference.getY());
                float distance = SlamManager.getInstance().getTwoPointDistance(point1.getX(), point1.getY(), point2.getX(), point2.getY());
                updateSpaceDistance(distance);
            }
        }
    }

    @Override
    public void onMoveTo(LocationBean data) {
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

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.tv_back, R.id.tv_location, R.id.tv_turn_left, R.id.tv_turn_right})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_location:
                locationClick();
                break;
            case R.id.tv_turn_left:
                moveClick(MoveDirection.TURN_LEFT);
                break;
            case R.id.tv_turn_right:
                moveClick(MoveDirection.TURN_RIGHT);
                break;
            default:
                break;
        }
    }

    private void locationClick() {
        if (!isAddPointViewDialogShow()) {
            mAddPointViewDialog = AddPointViewDialog.newInstance();
            mAddPointViewDialog.setOnPointListener(this);
            mAddPointViewDialog.setPose(mManualPose);
            mAddPointViewDialog.show(getSupportFragmentManager(), "ADD_POINT_DIALOG");
        }
    }

    private void moveClick(MoveDirection direction) {
        ThreadPoolManager.getInstance().execute(new MoveRunnable(direction));
    }

    private void updatePose() {
        if (mManualPose != null) {
            Pose pose = SlamManager.getInstance().getPose();
            mCurrentPose = pose;
            if (pose != null) {
                mManualPose.setYaw(pose.getYaw());
                mapView.setRobotPose(mManualPose);
                updatePoseShow(mManualPose.getX(), mManualPose.getY(), mManualPose.getYaw());
            }
        }
    }

    private void updatePoseShow(float x, float y, float yaw) {
        if (!isFinish) {
            float accuracy = 1000.0f;
            float finalX = Math.round(x * accuracy) / accuracy;
            float finalY = Math.round(y * accuracy) / accuracy;
            float finalYaw = Math.round(yaw * accuracy) / accuracy;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvPose.setText(getString(R.string.tv_pose, String.valueOf(finalX), String.valueOf(finalY), String.valueOf(finalYaw)));
                }
            });
        }
    }

    private void updateSpaceDistance(float value) {
        if (!isFinish) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvDistance.setText(getString(R.string.tv_distance, value));
                }
            });
        }
    }

    private void closeAddPointViewDialog() {
        if (isAddPointViewDialogShow()) {
            mAddPointViewDialog.getDialog().dismiss();
            mAddPointViewDialog = null;
        }
    }

    private boolean isAddPointViewDialogShow() {
        return mAddPointViewDialog != null && mAddPointViewDialog.getDialog() != null && mAddPointViewDialog.getDialog().isShowing();
    }

    private class MapHelper extends Thread {

        @Override
        public void run() {
            super.run();
            mapView.setMap(SlamManager.getInstance().getMap());
            mapView.setHomePose(SlamManager.getInstance().getHomePose());
            mCurrentPose = SlamManager.getInstance().getPose();
            mManualPose = mCurrentPose;
            mapView.setRobotPose(mCurrentPose);
            mapView.setLines(ArtifactUsage.ArtifactUsageVirtualWall, SlamManager.getInstance().getLines(ArtifactUsage.ArtifactUsageVirtualWall));
            mapView.setLines(ArtifactUsage.ArtifactUsageVirtualTrack, SlamManager.getInstance().getLines(ArtifactUsage.ArtifactUsageVirtualTrack));
            mapView.addLocationLabel(true, MyDBSource.getInstance(ManualAddLocationActivity.this).queryLocationList());
        }
    }

    private class MoveRunnable implements Runnable {
        private final MoveDirection direction;

        public MoveRunnable(MoveDirection direction) {
            this.direction = direction;
        }

        @Override
        public void run() {
            SlamManager.getInstance().moveBy(direction);
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(MSG_UPDATE, TIME_DELAY);
            }
        }
    }

    private static class ThreadHandler extends Handler {
        private final ManualAddLocationActivity mActivity;

        private ThreadHandler(WeakReference<ManualAddLocationActivity> reference, Looper looper) {
            super(looper);
            mActivity = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mActivity != null && msg.what == MSG_UPDATE) {
                mActivity.updatePose();
            }
        }
    }
}
