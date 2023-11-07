package com.tobot.map.module.task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.entity.RouteBean;
import com.tobot.map.module.common.GridItemDecoration;
import com.tobot.map.module.main.DataHelper;
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
    @BindView(R.id.tv_select_tips)
    TextView tvSelectTips;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.rl_bottom)
    RelativeLayout rlBottom;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_point)
    RecyclerView recyclerView;
    private static final int SPAN_COUNT = 6;
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
        TaskPointSelectAdapter adapter = new TaskPointSelectAdapter(this, R.layout.recycler_item_point);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        List<LocationBean> dataList = getIntent().getParcelableArrayListExtra(BaseConstant.DATA_KEY);
        adapter.setData(dataList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeTaskSaveDialog();
    }

    @Override
    public void onItemClick(int position, LocationBean data) {
        mSelectList.add(data);
        tvSelectTips.setText(DataHelper.getInstance().getSelectDetailTips(mSelectList));
        if (tvSelectTips.getVisibility() != View.VISIBLE) {
            tvSelectTips.setVisibility(View.VISIBLE);
            rlBottom.setVisibility(View.VISIBLE);
        }
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

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btn_confirm, R.id.btn_remove_point})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                showTaskSaveDialog(getString(R.string.tv_save_task), getString(R.string.tv_task_tips), getString(R.string.et_hint_task_tips));
                break;
            case R.id.btn_remove_point:
                removePoint();
                break;
            default:
                break;
        }
    }

    private void removePoint() {
        int index = mSelectList.size() - 1;
        if (index >= 0) {
            mSelectList.remove(index);
        }

        tvSelectTips.setText(DataHelper.getInstance().getSelectDetailTips(mSelectList));
        // 移除完的话隐藏显示
        if (index == 0) {
            tvSelectTips.setVisibility(View.GONE);
            rlBottom.setVisibility(View.GONE);
        }
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
