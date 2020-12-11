package com.tobot.map.module.set.device;

import android.graphics.RectF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.slam.SlamManager;
import com.tobot.slam.data.NetBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author houdeming
 * @date 2020/3/18
 */
public class DeviceInfoFragment extends BaseFragment {
    @BindView(R.id.recycler_device)
    RecyclerView recyclerView;
    private DeviceAdapter mDeviceAdapter;

    public static DeviceInfoFragment newInstance() {
        return new DeviceInfoFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_device_info;
    }

    @Override
    protected void init() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
        mDeviceAdapter = new DeviceAdapter(getActivity(), R.layout.recycler_item_device);
        recyclerView.setAdapter(mDeviceAdapter);
        if (SlamManager.getInstance().isConnected()) {
            new DeviceThread().start();
            return;
        }
        mDeviceAdapter.setData(getData());
    }

    private class DeviceThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                final String deviceId = SlamManager.getInstance().getDeviceId();
                final String slamVersion = SlamManager.getInstance().getSlamVersion();
                final String sdkVersion = SlamManager.getInstance().getSDKVersion();
                final NetBean netBean = SlamManager.getInstance().getNet();
                final boolean isDockingStatus = SlamManager.getInstance().isDockingStatus();
                final boolean isBatteryCharging = SlamManager.getInstance().isBatteryCharging();
                final RectF rectF = SlamManager.getInstance().getMap().getMapArea();

                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<DeviceBean> data = new ArrayList<>();
                        String[] deviceArray = getResources().getStringArray(R.array.device_list);
                        DeviceBean bean;
                        for (int i = 0, length = deviceArray.length; i < length; i++) {
                            bean = new DeviceBean();
                            bean.setName(deviceArray[i]);
                            // 按照顺序添加
                            switch (i) {
                                case 0:
                                    bean.setContent(deviceId);
                                    break;
                                case 1:
                                    bean.setContent(slamVersion);
                                    break;
                                case 2:
                                    bean.setContent(sdkVersion);
                                    break;
                                case 3:
                                    bean.setContent(netBean != null ? netBean.getMode() : getString(R.string.tv_unknown));
                                    break;
                                case 4:
                                    bean.setContent(netBean != null ? netBean.getSsid() : getString(R.string.tv_unknown));
                                    break;
                                case 5:
                                    bean.setContent(netBean != null ? netBean.getIp() : getString(R.string.tv_unknown));
                                    break;
                                case 6:
                                    String docking = isDockingStatus ? getString(R.string.tv_docking_true) : getString(R.string.tv_docking_false);
                                    String charge = isBatteryCharging ? getString(R.string.tv_charge_true) : getString(R.string.tv_charge_false);
                                    bean.setContent(getString(R.string.tv_charge_status_tips, docking, charge));
                                    break;
                                case 7:
                                    // 默认保留小数点后3位数
                                    bean.setContent(getString(R.string.tv_map_size_tips, getString(R.string.float_format, rectF.width()), getString(R.string.float_format, rectF.height())));
                                    break;
                                default:
                                    break;
                            }
                            data.add(bean);
                        }
                        mDeviceAdapter.setData(data);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<DeviceBean> getData() {
        List<DeviceBean> data = new ArrayList<>();
        String[] deviceArray = getResources().getStringArray(R.array.device_list);
        DeviceBean bean;
        for (String name : deviceArray) {
            bean = new DeviceBean();
            bean.setName(name);
            data.add(bean);
        }
        return data;
    }
}
