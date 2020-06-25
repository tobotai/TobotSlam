package com.tobot.map.module.common;

import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseV4Dialog;
import com.tobot.map.util.ToastUtils;

/**
 * @author houdeming
 * @date 2019/10/24
 */
public class NumberInputDialog extends BaseV4Dialog implements View.OnClickListener {
    private static final String CONTENT_KEY = "content_key";
    private static final String HINT_KEY = "hint_key";
    private TextView tvTitle, tvTips;
    private EditText editText;
    private OnNumberListener mOnNumberListener;

    public static NumberInputDialog newInstance(String titleTips, String contentTips, String hintTips) {
        NumberInputDialog dialog = new NumberInputDialog();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_KEY, titleTips);
        bundle.putString(CONTENT_KEY, contentTips);
        bundle.putString(HINT_KEY, hintTips);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_number_input;
    }

    @Override
    protected void initView(View view) {
        tvTitle = view.findViewById(R.id.tv_title);
        TextPaint paint = tvTitle.getPaint();
        paint.setFakeBoldText(true);
        tvTips = view.findViewById(R.id.tv_tips);
        editText = view.findViewById(R.id.et_number_input);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    @Override
    protected boolean isCanCancelByBack() {
        return true;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            tvTitle.setText(bundle.getString(DATA_KEY));
            tvTips.setText(bundle.getString(CONTENT_KEY));
            editText.setHint(bundle.getString(HINT_KEY));
        }
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;
            case R.id.btn_confirm:
                String content = editText.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {
                    ToastUtils.getInstance(getActivity()).show(getString(R.string.number_empty_tips));
                    return;
                }
                if (mOnNumberListener != null) {
                    mOnNumberListener.onNumber(content);
                }
                break;
            default:
                break;
        }
    }

    public void setOnNumberListener(OnNumberListener listener) {
        mOnNumberListener = listener;
    }

    public interface OnNumberListener {
        /**
         * 编号
         *
         * @param number
         */
        void onNumber(String number);
    }
}
