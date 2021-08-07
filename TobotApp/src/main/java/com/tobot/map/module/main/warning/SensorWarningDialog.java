package com.tobot.map.module.main.warning;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.main.DataHelper;

import java.util.List;

/**
 * @author houdeming
 * @date 2021/4/17
 */
public class SensorWarningDialog extends BaseDialog implements View.OnClickListener {
    private SensorWarningAdapter mAdapter;
    private List<WarningInfo> mWarningList;

    public static SensorWarningDialog newInstance(List<WarningInfo> data) {
        SensorWarningDialog dialog = new SensorWarningDialog();
        dialog.setData(data);
        return dialog;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_sensor_warning;
    }

    @Override
    protected void initView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new SensorWarningAdapter(getActivity(), R.layout.recycler_item_sensor_warning);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setData(mWarningList);
        view.findViewById(R.id.iv_clear).setOnClickListener(this);
        view.findViewById(R.id.iv_close).setOnClickListener(this);
    }

    @Override
    protected boolean isCanCancelByBack() {
        return true;
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_clear) {
            DataHelper.getInstance().clearWarningList();
            if (mAdapter != null) {
                mAdapter.setData(null);
            }
            return;
        }

        if (id == R.id.iv_close) {
            dismiss();
        }
    }

    private void setData(List<WarningInfo> data) {
        mWarningList = data;
    }
}
