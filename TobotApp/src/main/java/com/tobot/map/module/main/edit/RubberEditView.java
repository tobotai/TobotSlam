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
        view.findViewById(R.id.tv_back).setOnClickListener(this);
        radioGroup = view.findViewById(R.id.rg_wipe);
        view.findViewById(R.id.rb_wipe_white).setOnClickListener(this);
        view.findViewById(R.id.rb_wipe_grey).setOnClickListener(this);
        view.findViewById(R.id.rb_wipe_black).setOnClickListener(this);
        view.findViewById(R.id.rb_wipe_cancel).setOnClickListener(this);
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
            case R.id.rb_wipe_white:
                setRubberMode(Rubber.RUBBER_WHITE);
                break;
            case R.id.rb_wipe_grey:
                setRubberMode(Rubber.RUBBER_GREY);
                break;
            case R.id.rb_wipe_black:
                setRubberMode(Rubber.RUBBER_BLACK);
                break;
            case R.id.rb_wipe_cancel:
                if (mMapView != null) {
                    mMapView.closeRubber();
                }
                break;
            default:
                break;
        }
    }

    public void init(MapView mapView, OnEditListener listener) {
        mMapView = mapView;
        mOnEditListener = listener;
        radioGroup.clearCheck();
        setVisibility(VISIBLE);
        // 使用橡皮擦的时候，关闭更新地图
        SlamManager.getInstance().setMapUpdateAsync(false, null);
    }

    public void remove() {
        if (mMapView != null) {
            mMapView.closeRubber();
        }
        setVisibility(GONE);
    }

    private void setRubberMode(Rubber rubber) {
        if (mMapView != null) {
            mMapView.setRubberMode(rubber);
        }
    }
}
