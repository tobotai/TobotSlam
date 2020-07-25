package com.tobot.map.module.main.edit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.tobot.map.R;
import com.tobot.slam.SlamManager;

/**
 * @author houdeming
 * @date 2020/5/9
 */
public class RubberEditView extends LinearLayout implements View.OnClickListener {
    private RadioGroup radioGroup;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                callBackEditOption(OnEditListener.OPTION_CLOSE);
                break;
            case R.id.rb_wipe_white:
                callBackEditOption(OnEditListener.OPTION_WIPE_WHITE);
                break;
            case R.id.rb_wipe_grey:
                callBackEditOption(OnEditListener.OPTION_WIPE_GREY);
                break;
            case R.id.rb_wipe_black:
                callBackEditOption(OnEditListener.OPTION_WIPE_BLACK);
                break;
            case R.id.rb_wipe_cancel:
                callBackEditOption(OnEditListener.OPTION_WIPE_CANCEL);
                break;
            default:
                break;
        }
    }

    public void init(OnEditListener listener) {
        mOnEditListener = listener;
        radioGroup.clearCheck();
        setVisibility(VISIBLE);
        // 使用橡皮擦的时候，关闭更新地图
        SlamManager.getInstance().setMapUpdateInThread(false, null);
    }

    public void remove() {
        setVisibility(GONE);
    }

    private void callBackEditOption(int option) {
        if (mOnEditListener != null) {
            mOnEditListener.onEditOption(option);
        }
    }
}
