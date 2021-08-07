package com.tobot.map.module.set.device;

import android.graphics.RectF;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.util.ThreadPoolManager;
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
    private boolean isUpdateData;

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
            ThreadPoolManager.getInstance().execute(new DeviceRunnable());
            return;
        }

        mDeviceAdapter.setData(getData());
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && isUpdateData) {
            isUpdateData = false;
            ThreadPoolManager.getInstance().execute(new DeviceRunnable());
        }
    }

    private List<DeviceBean> getData() {
        List<DeviceBean> data = new ArrayList<>();
        String[] deviceArray = getResources().getStringArray(R.array.device_list);
        DeviceBean bean = new DeviceBean();
        for (String name : deviceArray) {
            bean = bean.clone();
            bean.setName(name);
            data.add(bean);
        }

        return data;
    }

    public void updateData() {
        isUpdateData = true;
    }

    private class DeviceRunnable implements Runnable {
        @Override
        public void run() {
            try {
                final String deviceId = SlamManager.getInstance().getDeviceId();
                final String slamVersion = SlamManager.getInstance().getSlamVersion();
                final String sdkVersion = SlamManager.getInstance().getSDKVersion();
                final String hardwareVersion = SlamManager.getInstance().getControlPanelHardwareVersion();
                final String softwareVersion = SlamManager.getInstance().getControlPanelSoftwareVersion();
                final String controlPanelSerial = SlamManager.getInstance().getControlPanelSerial();
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
                        String unknown = getString(R.string.unknown);
                        DeviceBean bean = new DeviceBean();
                        for (int i = 0, length = deviceArray.length; i < length; i++) {
                            bean = bean.clone();
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
                                    bean.setContent(hardwareVersion);
                                    break;
                                case 4:
                                    bean.setContent(softwareVersion);
                                    break;
                                case 5:
                                    bean.setContent(controlPanelSerial);
                                    break;
                                case 6:
                                    bean.setContent(netBean != null ? netBean.getMode() : unknown);
                                    break;
                                case 7:
                                    bean.setContent(netBean != null ? netBean.getSsid() : unknown);
                                    break;
                                case 8:
                                    bean.setContent(netBean != null ? netBean.getIp() : unknown);
                                    break;
                                case 9:
                                    String docking = isDockingStatus ? getString(R.string.tv_docking_true) : getString(R.string.tv_docking_false);
                                    String charge = isBatteryCharging ? getString(R.string.tv_charge_true) : getString(R.string.tv_charge_false);
                                    bean.setContent(getString(R.string.tv_charge_status_tips, docking, charge));
                                    break;
                                case 10:
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
}
