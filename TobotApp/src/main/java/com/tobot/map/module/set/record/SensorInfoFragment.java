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
import com.tobot.map.module.common.ItemSplitLineDecoration;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2022/06/24
 */
public class SensorInfoFragment extends BaseFragment {
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
    private SensorInfoAdapter mAdapter;

    public static SensorInfoFragment newInstance() {
        return new SensorInfoFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_record_sensor;
    }

    @Override
    protected void init() {
        List<RecordInfo> dataList = MyDBSource.getInstance(getActivity()).queryRecordWarningList();
        boolean isEmpty = dataList == null || dataList.isEmpty();
        btnDelete.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        llTips.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        viewLine.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (!isEmpty) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addItemDecoration(new ItemSplitLineDecoration(getActivity(), ItemSplitLineDecoration.VERTICAL, true));
            mAdapter = new SensorInfoAdapter(getActivity(), R.layout.recycler_item_record_sensor);
            recyclerView.setAdapter(mAdapter);
            mAdapter.setData(dataList);
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
            MyDBSource.getInstance(getActivity()).deleteAllRecordWarning();
            btnDelete.setVisibility(View.GONE);
            llTips.setVisibility(View.GONE);
            viewLine.setVisibility(View.GONE);
            if (mAdapter != null) {
                mAdapter.setData(null);
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
}
