package com.tobot.map.module.main.edit;

import android.content.Context;
import android.view.View;

import com.tobot.map.R;
import com.tobot.map.module.common.BasePopupWindow;

/**
 * 编辑
 *
 * @author houdeming
 * @date 2020/3/15
 */
public class EditPopupWindow extends BasePopupWindow {
    private OnEditListener mOnEditListener;

    public EditPopupWindow(Context context, OnEditListener listener) {
        super(context);
        mOnEditListener = listener;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.popup_edit;
    }

    @Override
    public void initView(View view) {
        view.findViewById(R.id.tv_virtual_wall).setOnClickListener(this);
        view.findViewById(R.id.tv_virtual_track).setOnClickListener(this);
        view.findViewById(R.id.tv_rubber).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_virtual_wall:
                handleClick(OnEditListener.TYPE_VIRTUAL_WALL);
                break;
            case R.id.tv_virtual_track:
                handleClick(OnEditListener.TYPE_VIRTUAL_TRACK);
                break;
            case R.id.tv_rubber:
                handleClick(OnEditListener.TYPE_RUBBER);
                break;
            default:
                break;
        }
    }

    private void handleClick(int type) {
        dismiss();
        if (mOnEditListener != null) {
            mOnEditListener.onEditClick(type);
        }
    }
}
