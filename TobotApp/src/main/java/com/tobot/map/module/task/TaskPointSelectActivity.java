package com.tobot.map.module.task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.entity.RouteBean;
import com.tobot.map.module.common.GridItemDecoration;
import com.tobot.map.util.ToastUtils;
import com.tobot.slam.data.LocationBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2020/3/17
 */
public class TaskPointSelectActivity extends BaseBackActivity implements BaseRecyclerAdapter.OnItemClickListener<LocationBean>, TaskSaveDialog.OnNameListener {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_select_all)
    TextView tvSelectAll;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_confirm)
    Button btnConfirm;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_point)
    RecyclerView recyclerView;
    private static final int SPAN_COUNT = 6;
    private TaskPointSelectAdapter mAdapter;
    private List<LocationBean> mAllData;
    private int mAllSize;
    private final List<LocationBean> mSelectList = new ArrayList<>();
    private TaskSaveDialog mTaskSaveDialog;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_task_point_select;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_create_task_title);
        recyclerView.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        int space = getResources().getDimensionPixelSize(R.dimen.item_split_size);
        recyclerView.addItemDecoration(new GridItemDecoration(space, space));
        mAdapter = new TaskPointSelectAdapter(this, R.layout.recycler_item_point);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        mAllData = getIntent().getParcelableArrayListExtra(BaseConstant.DATA_KEY);
        mAdapter.setData(mAllData);
        mAllSize = mAllData != null ? mAllData.size() : 0;
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeTaskSaveDialog();
    }

    @Override
    public void onItemClick(int position, LocationBean data) {
        if (mSelectList.contains(data)) {
            mSelectList.remove(data);
            if (mSelectList.isEmpty()) {
                btnConfirm.setEnabled(false);
            }
            // 取消全选
            if (tvSelectAll.isSelected()) {
                setSelect(false);
            }
        } else {
            mSelectList.add(data);
            if (!btnConfirm.isEnabled()) {
                btnConfirm.setEnabled(true);
            }
            // 设置全选
            if (mSelectList.size() == mAllSize) {
                setSelect(true);
            }
        }

        mAdapter.setSelectData(mSelectList);
    }

    @Override
    public void onName(String name) {
        RouteBean bean = MyDBSource.getInstance(this).queryRoute(name);
        if (bean == null) {
            bean = new RouteBean();
            bean.setRouteName(name);
            MyDBSource.getInstance(this).insertRoute(bean);
            MyDBSource.getInstance(this).insertRouteDetail(name, mSelectList);
            closeTaskSaveDialog();

            setResult(Activity.RESULT_OK, new Intent());
            finish();
            return;
        }

        ToastUtils.getInstance(this).show(R.string.task_name_exist_tips);
    }

    @OnClick({R.id.tv_select_all, R.id.btn_confirm})
    public void onClickView(View view) {
        int id = view.getId();
        if (id == R.id.tv_select_all) {
            mSelectList.clear();
            if (tvSelectAll.isSelected()) {
                tvSelectAll.setSelected(false);
            } else {
                tvSelectAll.setSelected(true);
                mSelectList.addAll(mAllData);
            }
            boolean isSelect = tvSelectAll.isSelected();
            setSelect(isSelect);
            btnConfirm.setEnabled(isSelect);
            mAdapter.setSelectData(mSelectList);
            return;
        }

        if (id == R.id.btn_confirm) {
            showTaskSaveDialog(getString(R.string.tv_save_task), getString(R.string.tv_task_tips), getString(R.string.et_hint_task_tips));
        }
    }

    private void setSelect(boolean isSelect) {
        tvSelectAll.setSelected(isSelect);
        tvSelectAll.setText(isSelect ? getString(R.string.tv_select_all_cancel) : getString(R.string.tv_select_all));
    }

    private void showTaskSaveDialog(String title, String contentTips, String hint) {
        if (!isTaskSaveDialogShow()) {
            mTaskSaveDialog = TaskSaveDialog.newInstance(title, contentTips, hint);
            mTaskSaveDialog.setOnNameListener(this);
            mTaskSaveDialog.show(getSupportFragmentManager(), "TASK_DIALOG");
        }
    }

    private void closeTaskSaveDialog() {
        if (isTaskSaveDialogShow()) {
            mTaskSaveDialog.getDialog().dismiss();
            mTaskSaveDialog = null;
        }
    }

    private boolean isTaskSaveDialogShow() {
        return mTaskSaveDialog != null && mTaskSaveDialog.getDialog() != null && mTaskSaveDialog.getDialog().isShowing();
    }
}
