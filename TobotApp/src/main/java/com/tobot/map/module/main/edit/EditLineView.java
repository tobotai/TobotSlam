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
    private static final int TYPE_ADD = 0;
    private static final int TYPE_CONFIRM = 1;
    private static final int TYPE_REMOVE = 2;
    private static final int TYPE_CLEAR = 3;
    private final RadioGroup radioGroup;
    private MapView mMapView;
    private AddLineView mAddLineView;
    private ArtifactUsage mArtifactUsage;
    private OnEditListener mOnEditListener;
    private int mLastCheckedId;
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
        radioGroup = view.findViewById(R.id.rg_line);
        view.findViewById(R.id.rb_back).setOnClickListener(this);
        view.findViewById(R.id.rb_add_line).setOnClickListener(this);
        view.findViewById(R.id.rb_confirm_line).setOnClickListener(this);
        view.findViewById(R.id.rb_remove_line).setOnClickListener(this);
        view.findViewById(R.id.rb_clear_line).setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_back:
                if (mOnEditListener != null) {
                    mOnEditListener.onEditClose();
                }
                break;
            case R.id.rb_add_line:
                radioButtonClick(TYPE_ADD);
                break;
            case R.id.rb_confirm_line:
                radioButtonClick(TYPE_CONFIRM);
                break;
            case R.id.rb_remove_line:
                radioButtonClick(TYPE_REMOVE);
                break;
            case R.id.rb_clear_line:
                radioButtonClick(TYPE_CLEAR);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position, LocationBean data) {
        if (data != null) {
            addLine(new PointF(data.getX(), data.getY()), mArtifactUsage);
        }
    }

    public void init(MapView mapView, AddLineView addLineView, ArtifactUsage artifactUsage, OnEditListener listener) {
        mMapView = mapView;
        mAddLineView = addLineView;
        mArtifactUsage = artifactUsage;
        mOnEditListener = listener;
        mLastCheckedId = -1;
        radioGroup.clearCheck();
        setVisibility(VISIBLE);
    }

    public void remove(boolean isGone) {
        if (mMapView != null) {
            mMapView.resetLine();
        }

        if (mAddLineView != null && mAddLineView.getVisibility() == VISIBLE) {
            mAddLineView.remove();
        }

        if (isGone) {
            setVisibility(GONE);
        }
    }

    private void radioButtonClick(int type) {
        int checkedId = radioGroup.getCheckedRadioButtonId();
        // 如果已经选中的话则取消选中
        if (mLastCheckedId == checkedId) {
            mLastCheckedId = -1;
            radioGroup.clearCheck();
            remove(false);
            return;
        }

        mLastCheckedId = checkedId;
        if (mMapView != null) {
            switch (type) {
                case TYPE_ADD:
                    showAddLineView(mArtifactUsage);
                    mMapView.setLine(mArtifactUsage);
                    break;
                case TYPE_CONFIRM:
                    mMapView.confirmDragLine(mArtifactUsage);
                    radioButtonClick(type);
                    break;
                case TYPE_REMOVE:
                    mMapView.removeLine(mArtifactUsage);
                    break;
                case TYPE_CLEAR:
                    mMapView.clearLines(mArtifactUsage);
                    radioButtonClick(type);
                    break;
                default:
                    break;
            }
        }
    }

    private void showAddLineView(ArtifactUsage artifactUsage) {
        if (mAddLineView != null && mAddLineView.getVisibility() != VISIBLE) {
            List<LocationBean> dataList = MyDBSource.getInstance(getContext()).queryLocationList();
            // 有位置点的话才显示，否则不显示
            if (dataList != null && !dataList.isEmpty()) {
                mAddLineView.init(dataList, this);
                isStart = false;
            }
        }
    }

    private void addLine(PointF pointF, ArtifactUsage artifactUsage) {
        if (isStart) {
            isStart = false;
            mPointEnd = pointF;
        } else {
            isStart = true;
            mPointStart = pointF;
        }

        mMapView.setLine(artifactUsage, pointF);
        if (!isStart) {
            SlamManager.getInstance().addLineAsync(artifactUsage, new Line(mPointStart, mPointEnd), null);
        }
    }
}
