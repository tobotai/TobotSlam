package com.tobot.map.module.set.record;

import android.annotation.SuppressLint;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.entity.SetBean;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.set.SetAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * @author houdeming
 * @date 2022/06/24
 */
public class RecordActivity extends BaseBackActivity implements BaseRecyclerAdapter.OnItemClickListener<SetBean> {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private static final int TAG_SENSOR_INFO = 0;
    private static final int TAG_WARNING_INFO = 1;
    private SetAdapter mAdapter;
    private SensorInfoFragment mSensorInfoFragment;
    private WarningInfoFragment mWarningInfoFragment;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_set;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_record);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(this, ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new SetAdapter(this, R.layout.recycler_item_set);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setData(getTagList());
        // 默认选中
        setSelectFragment(TAG_SENSOR_INFO);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (mSensorInfoFragment == null && fragment instanceof SensorInfoFragment) {
            mSensorInfoFragment = (SensorInfoFragment) fragment;
        }

        if (mWarningInfoFragment == null && fragment instanceof WarningInfoFragment) {
            mWarningInfoFragment = (WarningInfoFragment) fragment;
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
        SetBean bean = new SetBean(TAG_SENSOR_INFO, getString(R.string.tv_sensor));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_WARNING_INFO);
        bean.setName(getString(R.string.tv_warn));
        titles.add(bean);
        return titles;
    }

    private void setSelectFragment(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragment(transaction);
        switch (position) {
            case TAG_SENSOR_INFO:
                if (mSensorInfoFragment == null) {
                    mSensorInfoFragment = SensorInfoFragment.newInstance();
                    transaction.add(R.id.fl, mSensorInfoFragment);
                } else {
                    transaction.show(mSensorInfoFragment);
                }
                break;
            case TAG_WARNING_INFO:
                if (mWarningInfoFragment == null) {
                    mWarningInfoFragment = WarningInfoFragment.newInstance();
                    transaction.add(R.id.fl, mWarningInfoFragment);
                } else {
                    transaction.show(mWarningInfoFragment);
                }
                break;
            default:
                break;
        }

        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (mSensorInfoFragment != null) {
            transaction.hide(mSensorInfoFragment);
        }

        if (mWarningInfoFragment != null) {
            transaction.hide(mWarningInfoFragment);
        }
    }
}
