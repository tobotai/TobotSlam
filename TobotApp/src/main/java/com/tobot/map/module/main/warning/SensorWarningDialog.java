package com.tobot.map.module.main.warning;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tobot.map.R;
import com.tobot.map.base.BaseDialog;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.listener.OnResultListener;

import java.util.List;

/**
 * @author houdeming
 * @date 2021/4/17
 */
public class SensorWarningDialog extends BaseDialog implements View.OnClickListener, OnResultListener<Boolean> {
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
        view.findViewById(R.id.iv_delete).setOnClickListener(this);
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
        switch (v.getId()) {
            case R.id.iv_clear:
                SlamManager.getInstance().clearRobotHealthInfoAsync(this);
                break;
            case R.id.iv_delete:
                delete();
                break;
            case R.id.iv_close:
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResult(Boolean data) {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance(getContext()).show(data ? R.string.clear_success_tips : R.string.clear_fail_tips);
                }
            });
        }
    }

    private void setData(List<WarningInfo> data) {
        mWarningList = data;
    }

    private void delete() {
        DataHelper.getInstance().clearWarningList();
        if (mAdapter != null) {
            mAdapter.setData(null);
        }
        ToastUtils.getInstance(getContext()).show(R.string.delete_success);
    }
}
