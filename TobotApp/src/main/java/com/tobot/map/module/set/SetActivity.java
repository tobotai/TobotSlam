package com.tobot.map.module.set;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.entity.SetBean;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.set.device.DeviceInfoFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author houdeming
 * @date 2019/10/19
 */
public class SetActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemClickListener<SetBean> {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private static final int TAG_DEVICE_INFO = 0;
    private static final int TAG_CONFIG = 1;
    private static final int TAG_MAP_LIST = 2;
    private static final int TAG_TEST = 3;
    private static final int TAG_APP = 4;
    private SetAdapter mAdapter;
    private DeviceInfoFragment mDeviceInfoFragment;
    private ConfigFragment mConfigFragment;
    private MapListFragment mMapListFragment;
    private TestFragment mTestFragment;
    private AboutAppFragment mAboutAppFragment;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_set;
    }

    @Override
    protected void init() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(this, ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new SetAdapter(this, R.layout.recycler_item_set);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setData(getTagList());
        // 默认选中
        setSelectFragment(TAG_DEVICE_INFO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Logger.i(BaseConstant.TAG, "SetActivity onActivityResult() resultCode=" + resultCode);
        if (resultCode == BaseConstant.CODE_UPDATE_DEVICE_DATA) {
            if (mDeviceInfoFragment != null) {
                mDeviceInfoFragment.updateData();
            }
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (mDeviceInfoFragment == null && fragment instanceof DeviceInfoFragment) {
            mDeviceInfoFragment = (DeviceInfoFragment) fragment;
        }

        if (mConfigFragment == null && fragment instanceof ConfigFragment) {
            mConfigFragment = (ConfigFragment) fragment;
        }

        if (mMapListFragment == null && fragment instanceof MapListFragment) {
            mMapListFragment = (MapListFragment) fragment;
        }

        if (mTestFragment == null && fragment instanceof TestFragment) {
            mTestFragment = (TestFragment) fragment;
        }

        if (mAboutAppFragment == null && fragment instanceof AboutAppFragment) {
            mAboutAppFragment = (AboutAppFragment) fragment;
        }
    }

    @Override
    public void onItemClick(int position, SetBean data) {
        if (mAdapter != null) {
            mAdapter.setSelect(position);
        }
        setSelectFragment(data.getId());
    }

    private List<SetBean> getTagList() {
        List<SetBean> titles = new ArrayList<>();
        SetBean bean = new SetBean(TAG_DEVICE_INFO, getString(R.string.tv_device_info));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_CONFIG);
        bean.setName(getString(R.string.tv_config));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_MAP_LIST);
        bean.setName(getString(R.string.tv_map_list));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_TEST);
        bean.setName(getString(R.string.tv_test));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_APP);
        bean.setName(getString(R.string.tv_about_app));
        titles.add(bean);
        return titles;
    }

    private void setSelectFragment(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragment(transaction);
        switch (position) {
            case TAG_DEVICE_INFO:
                if (mDeviceInfoFragment == null) {
                    mDeviceInfoFragment = DeviceInfoFragment.newInstance();
                    transaction.add(R.id.fl, mDeviceInfoFragment);
                } else {
                    transaction.show(mDeviceInfoFragment);
                }
                break;
            case TAG_CONFIG:
                if (mConfigFragment == null) {
                    mConfigFragment = ConfigFragment.newInstance();
                    transaction.add(R.id.fl, mConfigFragment);
                } else {
                    transaction.show(mConfigFragment);
                }
                break;
            case TAG_MAP_LIST:
                if (mMapListFragment == null) {
                    mMapListFragment = MapListFragment.newInstance();
                    transaction.add(R.id.fl, mMapListFragment);
                } else {
                    transaction.show(mMapListFragment);
                }
                break;
            case TAG_TEST:
                if (mTestFragment == null) {
                    mTestFragment = TestFragment.newInstance();
                    transaction.add(R.id.fl, mTestFragment);
                } else {
                    transaction.show(mTestFragment);
                }
                break;
            case TAG_APP:
                if (mAboutAppFragment == null) {
                    mAboutAppFragment = AboutAppFragment.newInstance();
                    transaction.add(R.id.fl, mAboutAppFragment);
                } else {
                    transaction.show(mAboutAppFragment);
                }
                break;
            default:
                break;
        }

        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (mDeviceInfoFragment != null) {
            transaction.hide(mDeviceInfoFragment);
        }

        if (mConfigFragment != null) {
            transaction.hide(mConfigFragment);
        }

        if (mMapListFragment != null) {
            transaction.hide(mMapListFragment);
        }

        if (mTestFragment != null) {
            transaction.hide(mTestFragment);
        }

        if (mAboutAppFragment != null) {
            transaction.hide(mAboutAppFragment);
        }
    }
}
