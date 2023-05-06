package com.tobot.map.module.set.firmware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseRecyclerHolder;

/**
 * @author houdeming
 * @date 2021/03/11
 */
public class UpgradeFileAdapter extends BaseRecyclerAdapter<String> {
    private OnUpgradeListener<String> mListener;
    private int mSelectIndex = -1;

    public UpgradeFileAdapter(Context context, int itemLayoutId) {
        super(context, itemLayoutId);
    }

    @Override
    public void convert(BaseRecyclerHolder viewHolder, final String data, final int position) {
        TextView tvName = (TextView) viewHolder.getView(R.id.tv_name);
        Button btnUpgrade = (Button) viewHolder.getView(R.id.btn_upgrade);
        Button btnDelete = (Button) viewHolder.getView(R.id.btn_delete);

        if (!TextUtils.isEmpty(data)) {
            tvName.setText(data);
            tvName.setSelected(mSelectIndex == position);

            btnUpgrade.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onUpgrade(position, data);
                    }
                }
            });

            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onDelete(position, data);
                    }
                }
            });
        }
    }

    public void setOnUpgradeListener(OnUpgradeListener<String> listener) {
        mListener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSelectIndex(int position) {
        mSelectIndex = position;
        notifyDataSetChanged();
    }

    public interface OnUpgradeListener<T> {
        /**
         * 升级
         *
         * @param position
         * @param data
         */
        void onUpgrade(int position, T data);

        /**
         * 删除
         *
         * @param position
         * @param data
         */
        void onDelete(int position, T data);
    }
}
