package com.tobot.map.module.set.wifi;

import android.net.wifi.ScanResult;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tobot.map.R;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.base.BaseV4Dialog;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.util.NetworkUtils;

/**
 * WiFi列表
 *
 * @author houdeming
 * @date 2020/4/28
 */
public class WifiListDialog extends BaseV4Dialog {
    private BaseRecyclerAdapter.OnItemClickListener<ScanResult> mOnItemClickListener;

    public static WifiListDialog newInstance() {
        return new WifiListDialog();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_wifi_list;
    }

    @Override
    protected void initView(View view) {
        RecyclerView recyclerView = view.findViewById(R.id.recycler_wifi);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
        WifiAdapter adapter = new WifiAdapter(getActivity(), R.layout.recycler_item_wifi);
        adapter.setOnItemClickListener(mOnItemClickListener);
        recyclerView.setAdapter(adapter);
        adapter.setData(NetworkUtils.getWifiList(getActivity()));
    }

    @Override
    protected boolean isCanCancelByBack() {
        return true;
    }

    @Override
    protected double getScreenWidthPercentage() {
        return getResources().getInteger(R.integer.dialog_width_weight) / 10.0;
    }

    public void setOnItemClickListener(BaseRecyclerAdapter.OnItemClickListener<ScanResult> listener) {
        mOnItemClickListener = listener;
    }
}
