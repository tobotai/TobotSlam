package com.tobot.map.module.connect;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.db.MyDBSource;
import com.tobot.map.event.ConnectSlamEvent;
import com.tobot.map.event.ConnectSuccessEvent;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.common.PermissionHelper;
import com.tobot.map.module.main.DataHelper;
import com.tobot.map.module.main.MainActivity;
import com.tobot.map.module.main.MapService;
import com.tobot.map.module.set.SetActivity;
import com.tobot.map.util.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2019/10/18
 */
public class ConnectActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemClickListener<String> {
    @BindView(R.id.et_ip_input)
    EditText etIp;
    @BindView(R.id.recycler_ip)
    RecyclerView recyclerView;
    private ConnectIpAdapter mAdapter;
    private List<String> mIpList;
    private String mIp;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_connect;
    }

    @Override
    protected void init() {
        requestScreenSize();
        LogUtils.i("valueSize=" + getResources().getDimension(R.dimen.base_values));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(this, ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new ConnectIpAdapter(this, R.layout.recycler_item_ip);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        List<String> dataList = MyDBSource.getInstance(this).queryAllIp();
        mIpList = dataList;
        if (dataList != null && !dataList.isEmpty()) {
            mAdapter.setData(dataList);
        }
        DataHelper.getInstance().setLowBattery(0);
        // 默认没有查看过任何地图
        DataHelper.getInstance().setCurrentMapName("");
        // 默认数据库中不保存任何数据
        MyDBSource.getInstance(this).deleteAllLocation();
        EventBus.getDefault().register(this);
        startService(new Intent(getApplicationContext(), MapService.class));
        PermissionHelper.isRequestPermission(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        closeLoadTipsDialog();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 如果按了返回键就关闭service
        stopService(new Intent(getApplicationContext(), MapService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onItemClick(int position, String data) {
        etIp.setText(data);
        connect(data);
    }

    @OnClick({R.id.iv_set, R.id.btn_sta_connect, R.id.tv_delete})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.iv_set:
                startActivity(new Intent(this, SetActivity.class));
                break;
            case R.id.btn_sta_connect:
                String ip = etIp.getText().toString().trim();
                if (TextUtils.isEmpty(ip)) {
                    showToastTips(getString(R.string.ip_empty_tips));
                    return;
                }
                connect(ip);
                break;
            case R.id.tv_delete:
                if (mIpList != null && !mIpList.isEmpty()) {
                    mIpList.clear();
                    MyDBSource.getInstance(this).deleteAllIp();
                    mAdapter.setData(mIpList);
                    showToastTips(getString(R.string.delete_success));
                }
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectSuccessEvent(ConnectSuccessEvent event) {
        DataHelper.getInstance().setIp(mIp);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void connect(String ip) {
        mIp = ip;
        showLoadTipsDialog(getString(R.string.tv_connect_ing), null);
        EventBus.getDefault().post(new ConnectSlamEvent(ip));
    }

    private void requestScreenSize() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getRealMetrics(metrics);
        LogUtils.i("screenWidth=" + metrics.widthPixels + ",screenHeight=" + metrics.heightPixels);
    }
}
