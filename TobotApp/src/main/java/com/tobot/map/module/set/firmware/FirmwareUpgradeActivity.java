package com.tobot.map.module.set.firmware;

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
 * @date 2021/03/11
 */
public class FirmwareUpgradeActivity extends BaseBackActivity implements BaseRecyclerAdapter.OnItemClickListener<SetBean>, OnDataUpdateListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private static final int TAG_CONTROL_BOARD = 0;
    private static final int TAG_SLAM_MODULE = 1;
    private static final int TAG_DOWNLOAD = 2;
    private SetAdapter mAdapter;
    private ControlBoardFirmwareFragment mControlBoardFirmwareFragment;
    private SlamModuleFirmwareFragment mSlamModuleFirmwareFragment;
    private DownloadFragment mDownloadFragment;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_set;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.firmware_upgrade);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(this, ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new SetAdapter(this, R.layout.recycler_item_set);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setData(getTagList());
        // 默认选中
        setSelectFragment(TAG_CONTROL_BOARD);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (mControlBoardFirmwareFragment == null && fragment instanceof ControlBoardFirmwareFragment) {
            mControlBoardFirmwareFragment = (ControlBoardFirmwareFragment) fragment;
        }

        if (mSlamModuleFirmwareFragment == null && fragment instanceof SlamModuleFirmwareFragment) {
            mSlamModuleFirmwareFragment = (SlamModuleFirmwareFragment) fragment;
        }

        if (mDownloadFragment == null && fragment instanceof DownloadFragment) {
            mDownloadFragment = (DownloadFragment) fragment;
        }
    }

    @Override
    public void onItemClick(int position, SetBean data) {
        if (mAdapter != null) {
            mAdapter.setSelect(position);
        }
        setSelectFragment(data.getId());
    }

    @Override
    public void onDataUpdate() {
        if (mControlBoardFirmwareFragment != null) {
            mControlBoardFirmwareFragment.updateData();
        }
    }

    private List<SetBean> getTagList() {
        List<SetBean> titles = new ArrayList<>();
        SetBean bean = new SetBean(TAG_CONTROL_BOARD, getString(R.string.firmware_control_board));
        titles.add(bean);

//        bean = bean.clone();
//        bean.setId(TAG_SLAM_MODULE);
//        bean.setName(getString(R.string.firmware_slam_module));
//        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_DOWNLOAD);
        bean.setName(getString(R.string.firmware_download));
        titles.add(bean);
        return titles;
    }

    private void setSelectFragment(int position) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideFragment(transaction);
        switch (position) {
            case TAG_CONTROL_BOARD:
                if (mControlBoardFirmwareFragment == null) {
                    mControlBoardFirmwareFragment = ControlBoardFirmwareFragment.newInstance();
                    transaction.add(R.id.fl, mControlBoardFirmwareFragment);
                } else {
                    transaction.show(mControlBoardFirmwareFragment);
                }
                break;
            case TAG_SLAM_MODULE:
                if (mSlamModuleFirmwareFragment == null) {
                    mSlamModuleFirmwareFragment = SlamModuleFirmwareFragment.newInstance();
                    transaction.add(R.id.fl, mSlamModuleFirmwareFragment);
                } else {
                    transaction.show(mSlamModuleFirmwareFragment);
                }
                break;
            case TAG_DOWNLOAD:
                if (mDownloadFragment == null) {
                    mDownloadFragment = DownloadFragment.newInstance(this);
                    transaction.add(R.id.fl, mDownloadFragment);
                } else {
                    transaction.show(mDownloadFragment);
                }
                break;
            default:
                break;
        }

        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction) {
        if (mControlBoardFirmwareFragment != null) {
            transaction.hide(mControlBoardFirmwareFragment);
        }

        if (mSlamModuleFirmwareFragment != null) {
            transaction.hide(mSlamModuleFirmwareFragment);
        }

        if (mDownloadFragment != null) {
            transaction.hide(mDownloadFragment);
        }
    }
}
