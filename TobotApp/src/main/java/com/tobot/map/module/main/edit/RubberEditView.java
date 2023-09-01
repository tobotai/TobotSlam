package com.tobot.map.module.main.edit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.tobot.map.R;
import com.tobot.slam.SlamManager;
import com.tobot.slam.data.Rubber;
import com.tobot.slam.view.MapView;

/**
 * @author houdeming
 * @date 2020/5/9
 */
public class RubberEditView extends LinearLayout implements View.OnClickListener {
    private final RadioGroup radioGroup;
    private MapView mMapView;
    private int mPixelWidth, mLastCheckedId;
    private OnEditListener mOnEditListener;

    public RubberEditView(Context context) {
        this(context, null);
    }

    public RubberEditView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RubberEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_rubber_edit, this);
        radioGroup = view.findViewById(R.id.rg_wipe);
        view.findViewById(R.id.rb_back).setOnClickListener(this);
        view.findViewById(R.id.rb_wipe_white).setOnClickListener(this);
        view.findViewById(R.id.rb_wipe_grey).setOnClickListener(this);
        view.findViewById(R.id.rb_wipe_black).setOnClickListener(this);
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
            case R.id.rb_wipe_white:
                setRubberMode(Rubber.RUBBER_WHITE);
                break;
            case R.id.rb_wipe_grey:
                setRubberMode(Rubber.RUBBER_GREY);
                break;
            case R.id.rb_wipe_black:
                setRubberMode(Rubber.RUBBER_BLACK);
                break;
            default:
                break;
        }
    }

    public void init(MapView mapView, int pixelWidth, OnEditListener listener) {
        mMapView = mapView;
        mPixelWidth = pixelWidth;
        mOnEditListener = listener;
        mLastCheckedId = -1;
        radioGroup.clearCheck();
        setVisibility(VISIBLE);
        // 使用橡皮擦的时候，关闭更新地图
        SlamManager.getInstance().setMapUpdateAsync(false, null);
    }

    public void remove(boolean isGone) {
        if (mMapView != null) {
            mMapView.closeRubber();
        }

        if (isGone) {
            setVisibility(GONE);
        }
    }

    public void updatePixelWidth(int value) {
        mPixelWidth = value;
        if (mMapView != null) {
            mMapView.setPixelWidth(value);
        }
    }

    private void setRubberMode(Rubber rubber) {
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
            mMapView.setRubberMode(rubber, mPixelWidth);
        }
    }
}
