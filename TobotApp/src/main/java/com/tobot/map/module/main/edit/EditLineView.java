package com.tobot.map.module.main.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.db.MyDBSource;
import com.tobot.slam.SlamManager;
import com.tobot.slam.data.LocationBean;
import com.tobot.slam.view.MapView;

import java.util.List;

/**
 * @author houdeming
 * @date 2020/5/9
 */
public class EditLineView extends LinearLayout implements View.OnClickListener, BaseRecyclerAdapter.OnItemClickListener<LocationBean> {
    private final RadioGroup radioGroup;
    private int mEditType;
    private MapView mMapView;
    private AddLineView mAddLineView;
    private OnEditListener mOnEditListener;
    private boolean isStart;
    private PointF mPointStart, mPointEnd;

    public EditLineView(Context context) {
        this(context, null);
    }

    public EditLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_line_edit, this);
        view.findViewById(R.id.tv_back).setOnClickListener(this);
        radioGroup = view.findViewById(R.id.rg_line);
        view.findViewById(R.id.rb_add_line).setOnClickListener(this);
        view.findViewById(R.id.rb_remove_line).setOnClickListener(this);
        view.findViewById(R.id.rb_clear_line).setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                if (mOnEditListener != null) {
                    mOnEditListener.onEditClose();
                }
                break;
            case R.id.rb_add_line:
                ArtifactUsage artifactUsage = getArtifactUsage();
                showAddLineView(artifactUsage);
                if (mMapView != null) {
                    mMapView.setLine(artifactUsage);
                }
                break;
            case R.id.rb_remove_line:
                if (mMapView != null) {
                    mMapView.removeLine(getArtifactUsage());
                }
                break;
            case R.id.rb_clear_line:
                if (mMapView != null) {
                    mMapView.clearLines(getArtifactUsage());
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position, LocationBean data) {
        if (data != null) {
            addLine(new PointF(data.getX(), data.getY()));
        }
    }

    public void init(int editType, MapView mapView, AddLineView view, OnEditListener listener) {
        mEditType = editType;
        mMapView = mapView;
        mAddLineView = view;
        mOnEditListener = listener;
        radioGroup.clearCheck();
        setVisibility(VISIBLE);
    }

    public void remove() {
        if (mMapView != null) {
            mMapView.resetLine();
        }

        setVisibility(GONE);
        if (mAddLineView != null && mAddLineView.getVisibility() == VISIBLE) {
            mAddLineView.remove();
        }
    }

    private ArtifactUsage getArtifactUsage() {
        return mEditType == OnEditListener.TYPE_VIRTUAL_WALL ? ArtifactUsage.ArtifactUsageVirutalWall : ArtifactUsage.ArtifactUsageVirtualTrack;
    }

    private void showAddLineView(ArtifactUsage artifactUsage) {
        // 只有添加虚拟轨道的时候显示
        if (artifactUsage == ArtifactUsage.ArtifactUsageVirutalWall) {
            return;
        }

        if (mAddLineView != null && mAddLineView.getVisibility() != VISIBLE) {
            List<LocationBean> dataList = MyDBSource.getInstance(getContext()).queryLocationList();
            // 有位置点的话才显示，否则不显示
            if (dataList != null && !dataList.isEmpty()) {
                mAddLineView.init(dataList, this);
                isStart = false;
            }
        }
    }

    private void addLine(PointF pointF) {
        if (isStart) {
            isStart = false;
            mPointEnd = pointF;
        } else {
            isStart = true;
            mPointStart = pointF;
        }

        mMapView.setLine(getArtifactUsage(), pointF);
        if (!isStart) {
            SlamManager.getInstance().addLineAsync(getArtifactUsage(), new Line(mPointStart, mPointEnd), null);
        }
    }
}
