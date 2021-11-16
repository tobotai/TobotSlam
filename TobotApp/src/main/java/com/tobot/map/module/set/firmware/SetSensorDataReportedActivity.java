package com.tobot.map.module.set.firmware;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.entity.SetBean;
import com.tobot.map.module.common.ItemSplitLineDecoration;
import com.tobot.map.module.set.SetAdapter;
import com.tobot.map.util.SystemUtils;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.firmware.IdType;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2021/03/09
 */
public class SetSensorDataReportedActivity extends BaseActivity implements BaseRecyclerAdapter.OnItemClickListener<SetBean> {
    @BindView(R.id.tv_head)
    TextView tvHead;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.et_num)
    EditText etNum;
    @BindView(R.id.tv_sensor_sonar)
    TextView tvSonar;
    @BindView(R.id.tv_sensor_cliff)
    TextView tvCliff;
    @BindView(R.id.tv_sensor_bumper)
    TextView tvBumper;
    private static final int TAG_SONAR = 0;
    private static final int TAG_CLIFF = 1;
    private static final int TAG_BUMPER = 2;
    private SetAdapter mAdapter;
    private int mSelectId = TAG_SONAR;
    private boolean isOpen = true;
    private static final int ALL_NUM = -1;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_set_sensor_data_reported;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.set_sensor_status);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new ItemSplitLineDecoration(this, ItemSplitLineDecoration.VERTICAL, true));
        mAdapter = new SetAdapter(this, R.layout.recycler_item_set);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        List<SetBean> data = getTagList();
        mAdapter.setData(data);
        // 默认选中第一个
        mAdapter.setSelect(mSelectId);
        String unKnown = getString(R.string.unknown);
        tvSonar.setText(getString(R.string.sensor_sonar, unKnown));
        tvCliff.setText(getString(R.string.sensor_cliff, unKnown));
        tvBumper.setText(getString(R.string.sensor_bumper, unKnown));
        ThreadPoolManager.getInstance().execute(new GetRunnable());
    }

    @Override
    public void onItemClick(int position, SetBean data) {
        if (data != null) {
            mSelectId = data.getId();
        }

        if (mAdapter != null) {
            mAdapter.setSelect(position);
        }
    }

    @OnClick({R.id.rb_open, R.id.rb_close, R.id.btn_send})
    public void onClickView(View v) {
        switch (v.getId()) {
            case R.id.rb_open:
                isOpen = true;
                break;
            case R.id.rb_close:
                isOpen = false;
                break;
            case R.id.btn_send:
                send();
                break;
            default:
                break;
        }
    }

    private List<SetBean> getTagList() {
        List<SetBean> titles = new ArrayList<>();
        SetBean bean = new SetBean(TAG_SONAR, getString(R.string.sonar));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_CLIFF);
        bean.setName(getString(R.string.cliff));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_BUMPER);
        bean.setName(getString(R.string.bumper));
        titles.add(bean);
        return titles;
    }

    private void send() {
        SystemUtils.hideKeyboard(this);
        String content = etNum.getText().toString().trim();
        // 执行全部
        if (TextUtils.isEmpty(content)) {
            ThreadPoolManager.getInstance().execute(new SetRunnable(mSelectId, ALL_NUM));
            return;
        }

        if (!TextUtils.isDigitsOnly(content)) {
            showToastTips(getString(R.string.sensor_num_not_digits_tips));
            return;
        }

        ThreadPoolManager.getInstance().execute(new SetRunnable(mSelectId, Integer.parseInt(content)));
    }

    private void setSensorTips(int id, List<Integer> list) {
        String content = getString(R.string.unknown);
        if (list != null && !list.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0, size = list.size(); i < size; i++) {
                if (size > 1) {
                    builder.append(i);
                    builder.append(BaseConstant.SENSOR_STATUS_SPLIT);
                }

                builder.append(getString(list.get(i) == 1 ? R.string.btn_open : R.string.btn_close));
                if (i != size - 1) {
                    builder.append(BaseConstant.SPLIT);
                }
            }

            content = builder.toString().trim();
        }

        String tips = content;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (id) {
                    case TAG_SONAR:
                        tvSonar.setText(getString(R.string.sensor_sonar, tips));
                        break;
                    case TAG_CLIFF:
                        tvCliff.setText(getString(R.string.sensor_cliff, tips));
                        break;
                    case TAG_BUMPER:
                        tvBumper.setText(getString(R.string.sensor_bumper, tips));
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private class SetRunnable implements Runnable {
        private int mSelectId;
        private int mNum;

        private SetRunnable(int selectId, int num) {
            mSelectId = selectId;
            mNum = num;
        }

        @Override
        public void run() {
            boolean isSuccess;
            if (mNum == ALL_NUM) {
                isSuccess = SlamManager.getInstance().setDefinedAllSensorDataReported(getIdType(mSelectId), isOpen);
            } else {
                isSuccess = SlamManager.getInstance().setDefinedSensorDataReported(getIdType(mSelectId), mNum, isOpen);
            }

            showToastTips(getString(R.string.set_sensor_result, isSuccess));
            // 重新去查询上报状态
            switch (mSelectId) {
                case TAG_SONAR:
                    setSensorTips(mSelectId, SlamManager.getInstance().getDefinedSensorDataReported(IdType.SONAR));
                    break;
                case TAG_CLIFF:
                    setSensorTips(mSelectId, SlamManager.getInstance().getDefinedSensorDataReported(IdType.CLIFF));
                    break;
                case TAG_BUMPER:
                    setSensorTips(mSelectId, SlamManager.getInstance().getDefinedSensorDataReported(IdType.BUMPER));
                    break;
                default:
                    break;
            }
        }

        private IdType getIdType(int index) {
            IdType type = IdType.SONAR;
            switch (index) {
                case TAG_SONAR:
                    type = IdType.SONAR;
                    break;
                case TAG_CLIFF:
                    type = IdType.CLIFF;
                    break;
                case TAG_BUMPER:
                    type = IdType.BUMPER;
                    break;
                default:
                    break;
            }

            return type;
        }
    }

    private class GetRunnable implements Runnable {

        @Override
        public void run() {
            setSensorTips(TAG_SONAR, SlamManager.getInstance().getDefinedSensorDataReported(IdType.SONAR));
            setSensorTips(TAG_CLIFF, SlamManager.getInstance().getDefinedSensorDataReported(IdType.CLIFF));
            setSensorTips(TAG_BUMPER, SlamManager.getInstance().getDefinedSensorDataReported(IdType.BUMPER));
        }
    }
}
