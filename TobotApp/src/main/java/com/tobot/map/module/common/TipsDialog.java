package com.tobot.map.module.common;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;

/**
 * @author houdeming
 * @date 2020/4/28
 */
public class TipsDialog extends BaseDialog implements View.OnClickListener {
    private TextView tvTips;
    private String mContent;
    private OnConfirmListener mOnConfirmListener;

    public static TipsDialog newInstance(String tips) {
        TipsDialog dialog = new TipsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_KEY, tips);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_tips;
    }

    @Override
    protected void initView(View view) {
        tvTips = view.findViewById(R.id.tv_tips);
        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    @Override
    protected boolean isCanCancelByBack() {
        return false;
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mContent = bundle.getString(DATA_KEY);
            tvTips.setText(mContent);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_confirm) {
            dismiss();
            if (mOnConfirmListener != null) {
                mOnConfirmListener.onConfirm();
            }
        }
    }

    public void setContent(String content) {
        if (tvTips != null) {
            if (!TextUtils.isEmpty(mContent)) {
                if (mContent.contains(content)) {
                    return;
                }

                // 继续追加内容
                content = mContent + "，" + content;
            }

            mContent = content;
            tvTips.setText(content);
        }
    }

    public void setOnConfirmListener(OnConfirmListener listener) {
        mOnConfirmListener = listener;
    }

    public interface OnConfirmListener {
        /**
         * 确认
         */
        void onConfirm();
    }
}
