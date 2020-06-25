package com.tobot.map.module.set;

import android.support.v4.app.Fragment;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.module.set.device.DeviceInfoFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import q.rorbin.verticaltablayout.VerticalTabLayout;

/**
 * @author houdeming
 * @date 2019/10/19
 */
public class SetActivity extends BaseActivity {
    @BindView(R.id.tab_layout)
    VerticalTabLayout tabLayout;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_set;
    }

    @Override
    protected void init() {
        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.tv_device_info));
        titles.add(getString(R.string.tv_config));
        titles.add(getString(R.string.tv_low_battery));
        titles.add(getString(R.string.tv_map_list));
        // 与上面的title要一一对应
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(DeviceInfoFragment.newInstance());
        fragments.add(ConfigFragment.newInstance());
        fragments.add(LowBatteryFragment.newInstance());
        fragments.add(MapListFragment.newInstance());

        tabLayout.setupWithFragment(getSupportFragmentManager(), R.id.fl_layout, fragments, new SetAdapter(this, titles));
        tabLayout.setTabSelected(0);
    }
}
