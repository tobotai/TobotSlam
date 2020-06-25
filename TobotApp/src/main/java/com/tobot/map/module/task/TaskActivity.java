package com.tobot.map.module.task;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.base.BaseConstant;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.entity.RouteBean;
import com.tobot.map.util.LogUtils;
import com.tobot.slam.data.LocationBean;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2020/3/16
 */
public class TaskActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemClickListener<RouteBean>, TaskAdapter.OnItemLongClickListener,
        TaskDetailDialog.OnDeleteListener, TaskExecuteConfirmDialog.OnExecuteListener {
    @BindView(R.id.tv_task_tips)
    TextView tvTips;
    @BindView(R.id.recycler_route)
    RecyclerView recyclerView;
    private static final int SPAN_COUNT = 5;
    private TaskAdapter mAdapter;
    private TaskDetailDialog mTaskDetailDialog;
    private TaskExecuteConfirmDialog mTaskExecuteConfirmDialog;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_task;
    }

    @Override
    protected void init() {
        recyclerView.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        int space = getResources().getDimensionPixelSize(R.dimen.item_split_size);
        recyclerView.addItemDecoration(new GridItemDecoration(space, space));
        mAdapter = new TaskAdapter(this, R.layout.recycler_item_task);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnItemLongClickListener(this);
        recyclerView.setAdapter(mAdapter);
        setData(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            setData(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTaskDetailDialogShow()) {
            mTaskDetailDialog.getDialog().dismiss();
            mTaskDetailDialog = null;
        }
        if (isTaskExecuteConfirmDialogShow()) {
            mTaskExecuteConfirmDialog.getDialog().dismiss();
            mTaskExecuteConfirmDialog = null;
        }
    }

    @Override
    public void onItemClick(int position, RouteBean data) {
        if (data != null && !isTaskExecuteConfirmDialogShow()) {
            mTaskExecuteConfirmDialog = TaskExecuteConfirmDialog.newInstance(data.getRouteName());
            mTaskExecuteConfirmDialog.setOnExecuteListener(this);
            mTaskExecuteConfirmDialog.show(getFragmentManager(), "TASK_EXECUTE_CONFIRM_DIALOG");
        }
    }

    @Override
    public void onItemLongClick(int position, RouteBean data) {
        if (data != null && !isTaskDetailDialogShow()) {
            mTaskDetailDialog = TaskDetailDialog.newInstance(data.getRouteName());
            mTaskDetailDialog.setOnDeleteListener(this);
            mTaskDetailDialog.show(getFragmentManager(), "TASK_DETAIL_DIALOG");
        }
    }

    @Override
    public void onDelete() {
        setData(true);
    }

    @Override
    public void onExecute(List<LocationBean> locationBeans, int loopCount) {
        LogUtils.i("loopCount=" + loopCount);
        Intent data = new Intent();
        data.putParcelableArrayListExtra(BaseConstant.DATA_KEY, (ArrayList<? extends Parcelable>) locationBeans);
        data.putExtra(BaseConstant.LOOP_KEY, loopCount);
        setResult(Activity.RESULT_OK, data);
        finish();
    }

    @OnClick({R.id.tv_create_task})
    public void onClickView(View view) {
        createTask();
    }

    private void setData(boolean isDelete) {
        List<RouteBean> beanList = MyDBSource.getInstance(this).queryRoute();
        mAdapter.setData(beanList);
        // 数据不为空的话显示提示
        if (beanList != null && !beanList.isEmpty()) {
            tvTips.setVisibility(View.VISIBLE);
            return;
        }
        tvTips.setVisibility(View.GONE);
        if (isDelete) {
            MyDBSource.getInstance(this).clearRouteId();
        }
    }

    private void createTask() {
        List<LocationBean> data = MyDBSource.getInstance(this).queryLocation();
        if (data != null && !data.isEmpty()) {
            Intent intent = new Intent(this, TaskPointSelectActivity.class);
            intent.putParcelableArrayListExtra(BaseConstant.DATA_KEY, (ArrayList<? extends Parcelable>) data);
            startActivityForResult(intent, 1);
            return;
        }
        showToastTips(getString(R.string.task_point_empty_tips));
    }

    private boolean isTaskDetailDialogShow() {
        return mTaskDetailDialog != null && mTaskDetailDialog.getDialog() != null && mTaskDetailDialog.getDialog().isShowing();
    }

    private boolean isTaskExecuteConfirmDialogShow() {
        return mTaskExecuteConfirmDialog != null && mTaskExecuteConfirmDialog.getDialog() != null && mTaskExecuteConfirmDialog.getDialog().isShowing();
    }
}
