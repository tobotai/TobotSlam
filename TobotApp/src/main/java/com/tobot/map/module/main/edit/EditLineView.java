package com.tobot.map.module.main.edit;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.tobot.map.R;
import com.tobot.map.db.MyDBSource;
import com.tobot.slam.data.LocationBean;

import java.util.List;

/**
 * @author houdeming
 * @date 2020/5/9
 */
public class EditLineView extends LinearLayout implements View.OnClickListener {
    private RadioGroup radioGroup;
    private int mEditType;
    private AddLineView mAddLineView;
    private OnEditListener mOnEditListener;

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
                callBackEditOption(OnEditListener.OPTION_CLOSE);
                break;
            case R.id.rb_add_line:
                callBackEditOption(OnEditListener.OPTION_ADD);
                showAddLineView();
                break;
            case R.id.rb_remove_line:
                callBackEditOption(OnEditListener.OPTION_REMOVE);
                break;
            case R.id.rb_clear_line:
                callBackEditOption(OnEditListener.OPTION_CLEAR);
                break;
            default:
                break;
        }
    }

    public void init(int editType, AddLineView view, OnEditListener listener) {
        mEditType = editType;
        mAddLineView = view;
        mOnEditListener = listener;
        radioGroup.clearCheck();
        setVisibility(VISIBLE);
    }

    public void remove() {
        setVisibility(GONE);
        if (mAddLineView != null && mAddLineView.getVisibility() == VISIBLE) {
            mAddLineView.remove();
        }
    }

    private void callBackEditOption(int option) {
        if (mOnEditListener != null) {
            mOnEditListener.onEditOption(option);
        }
    }

    private void showAddLineView() {
        // 只有添加虚拟轨道的时候显示
        if (mEditType == OnEditListener.TYPE_VIRTUAL_TRACK && mAddLineView != null && mAddLineView.getVisibility() != VISIBLE) {
            List<LocationBean> dataList = MyDBSource.getInstance(getContext()).queryLocationList();
            // 有位置点的话才显示，否则不显示
            if (dataList != null && !dataList.isEmpty()) {
                mAddLineView.init(dataList, mOnEditListener);
            }
        }
    }
}
