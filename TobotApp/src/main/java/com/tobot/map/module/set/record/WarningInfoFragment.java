package com.tobot.map.module.set.record;

import android.annotation.SuppressLint;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.tobot.map.R;
import com.tobot.map.base.BaseFragment;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.entity.RecordInfo;
import com.tobot.map.module.common.AbstractLoadMore;
import com.tobot.map.module.common.ItemSplitLineDecoration;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2022/06/24
 */
public class WarningInfoFragment extends BaseFragment {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_delete)
    Button btnDelete;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.ll_tips)
    LinearLayout llTips;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.view_line)
    View viewLine;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    public static final int PAGE_COUNT = 10;
    private WarningInfoAdapter mAdapter;
    private final List<RecordInfo> mInfoList = new ArrayList<>();
    private int mCurrentIndex;

    public static WarningInfoFragment newInstance() {
        return new WarningInfoFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record_info;
    }

    @Override
    protected void init() {
        List<RecordInfo> infoList = getData(mCurrentIndex);
        boolean isEmpty = infoList == null || infoList.isEmpty();
        btnDelete.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        llTips.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        viewLine.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (!isEmpty) {
            mInfoList.addAll(infoList);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
            mAdapter = new WarningInfoAdapter(getActivity(), R.layout.recycler_item_record_info);
            recyclerView.setAdapter(mAdapter);
            mAdapter.setData(mInfoList);
            recyclerView.addOnScrollListener(new AbstractLoadMore() {

                @Override
                protected void onLoadMore(int lastItem) {
                    handleLoadMore(lastItem);
                }
            });
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeConfirmDialog();
    }

    @Override
    public void onConfirm(boolean isConfirm) {
        super.onConfirm(isConfirm);
        if (isConfirm) {
            MyDBSource.getInstance(getActivity()).deleteAllRecordInfo();
            btnDelete.setVisibility(View.GONE);
            llTips.setVisibility(View.GONE);
            viewLine.setVisibility(View.GONE);
            mCurrentIndex = 0;
            if (mInfoList != null && !mInfoList.isEmpty()) {
                mInfoList.clear();
            }

            if (mAdapter != null) {
                mAdapter.setData(mInfoList);
            }
            showToastTips(getString(R.string.delete_success));
        }
    }

    @OnClick({R.id.btn_delete})
    public void onClickView(View v) {
        if (v.getId() == R.id.btn_delete) {
            showConfirmDialog(getString(R.string.delete_all_tips));
        }
    }

    private void handleLoadMore(int lastItem) {
        mCurrentIndex = mCurrentIndex + PAGE_COUNT;
        List<RecordInfo> infoList = getData(mCurrentIndex);
        if (infoList == null || infoList.isEmpty()) {
            return;
        }

        mInfoList.addAll(infoList);
        if (mAdapter != null) {
            mAdapter.setData(mInfoList);
        }
    }

    private List<RecordInfo> getData(int pageIndex) {
        return MyDBSource.getInstance(getActivity()).queryRecordInfoList(pageIndex, PAGE_COUNT);
    }
}
