package com.tobot.map.module.main.edit;

import android.annotation.SuppressLint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.slamtec.slamware.robot.SensorType;
import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.module.log.Logger;
import com.tobot.slam.agent.DragTouchListener;
import com.tobot.slam.view.MapView;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2023/04/25
 */
public class EditMapActivity extends BaseActivity implements MapView.OnMapListener, OnEditListener, BaseBar.OnSeekBarChangeListener, DragTouchListener.OnTouchPointListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.map_view)
    MapView mapView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_control)
    LinearLayout llControl;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_edit_line)
    EditLineView editLineView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_rubber_edit)
    RubberEditView rubberEditView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_add_line)
    AddLineView addLineView;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_pixel)
    LinearLayout llPixel;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_current_pixel)
    TextView tvPixel;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.sb_pixel)
    StripSeekBar sbPixel;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_point1)
    TextView tvPoint1;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_point2)
    TextView tvPoint2;
    private static final int TYPE_VIRTUAL_WALL = 0;
    private static final int TYPE_VIRTUAL_TRACK = 1;
    private static final int TYPE_RUBBER = 2;
    private static final int PIXEL_MAX = 100;
    private EditMapHelper mMapHelper;
    private int mPixelWidth = 0;
    private PointF mPointStart, mPointEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_edit_map;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void init() {
        mapView.setOnMapListener(this);
        mMapHelper = new EditMapHelper(new WeakReference<>(mapView));
        mapView.addLocationLabel(true, MyDBSource.getInstance(this).queryLocationList());
        sbPixel.setOnSeekBarChangeListener(this);
        tvPixel.setText(getString(R.string.pixel_width, mPixelWidth));
        sbPixel.setProgress(mPixelWidth * 1.0f / PIXEL_MAX);
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        Logger.i(BaseConstant.TAG, "screenWidth=" + screenWidth + ",screenHeight=" + screenHeight);
        DragTouchListener dragTouchListener = new DragTouchListener(screenWidth, screenHeight, this);
        tvPoint1.setOnTouchListener(dragTouchListener);
        tvPoint2.setOnTouchListener(dragTouchListener);
    }

    @Override
    public void onBackPressed() {
        if (editLineView.getVisibility() == View.VISIBLE) {
            removeEditLineView();
            return;
        }

        if (rubberEditView.getVisibility() == View.VISIBLE) {
            removeRubberView();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMapHelper != null) {
            mMapHelper.startUpdateMap();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMapHelper != null) {
            mMapHelper.stopUpdateMap();
            mMapHelper = null;
        }
    }

    @Override
    public void onMapClick(MotionEvent event) {
    }

    @Override
    public void onSensorStatus(SensorType sensorType, int id, boolean isTrigger) {
    }

    @Override
    public void onHealthInfo(int code, String info) {
    }

    @Override
    public void onRelocationArea(RectF area) {
    }

    @Override
    public void onEditClose() {
        if (editLineView.getVisibility() == View.VISIBLE) {
            setDragPointShow(false, false);
            removeEditLineView();
            return;
        }

        removeRubberView();
    }

    @Override
    public void onSeekBarStart(View view) {
    }

    @Override
    public void onProgressChange(View view, float progress) {
        setProgress(view, progress);
    }

    @Override
    public void onSeekBarStop(View view, float progress) {
        setProgress(view, progress);
        if (rubberEditView.getVisibility() == View.VISIBLE) {
            rubberEditView.updatePixelWidth(mPixelWidth);
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
                mPointStart = new PointF(x, y);
                break;
            case R.id.tv_point2:
                isHandle = true;
                tvPoint2.setText("");
                mPointEnd = new PointF(x, y);
                break;
            default:
                break;
        }

        if (isHandle) {
            mapView.setDragLine(mPointStart, mPointEnd);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.tv_back, R.id.tv_virtual_wall, R.id.tv_virtual_track, R.id.tv_rubber})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                finish();
                break;
            case R.id.tv_virtual_wall:
                editClick(TYPE_VIRTUAL_WALL);
                break;
            case R.id.tv_virtual_track:
                editClick(TYPE_VIRTUAL_TRACK);
                break;
            case R.id.tv_rubber:
                editClick(TYPE_RUBBER);
                break;
            default:
                break;
        }
    }

    private void setDragPointShow(boolean isShow, boolean isResetLine) {
        tvPoint1.setVisibility(isShow ? View.VISIBLE : View.GONE);
        tvPoint2.setVisibility(isShow ? View.VISIBLE : View.GONE);
        if (isResetLine) {
            mPointStart = null;
            mPointEnd = null;
            mapView.setDragLine(null, null);
        }
    }

    private void editClick(int type) {
        llControl.setVisibility(View.GONE);
        if (type == TYPE_RUBBER) {
            llPixel.setVisibility(View.VISIBLE);
            rubberEditView.init(mapView, mPixelWidth, this);
            setDragPointShow(false, true);
            return;
        }

        ArtifactUsage artifactUsage = type == TYPE_VIRTUAL_WALL ? ArtifactUsage.ArtifactUsageVirtualWall : ArtifactUsage.ArtifactUsageVirtualTrack;
        editLineView.init(mapView, addLineView, artifactUsage, this);
        setDragPointShow(true, false);
    }

    private void setProgress(View view, float progress) {
        int value = (int) (progress * PIXEL_MAX);
        mPixelWidth = value;
        tvPixel.setText(getString(R.string.pixel_width, value));
    }

    private void removeEditLineView() {
        editLineView.remove(true);
        llControl.setVisibility(View.VISIBLE);
    }

    private void removeRubberView() {
        rubberEditView.remove(true);
        llPixel.setVisibility(View.GONE);
        llControl.setVisibility(View.VISIBLE);
    }
}
