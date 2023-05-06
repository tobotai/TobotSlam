package com.tobot.map.module.set.firmware;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseBackActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.entity.SetBean;
import com.tobot.map.module.common.GridItemDecoration;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.firmware.IdType;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2021/03/09
 */
public class SensorInfoActivity extends BaseBackActivity implements BaseBar.OnSeekBarChangeListener, BaseRecyclerAdapter.OnItemClickListener<SetBean> {
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_head)
    TextView tvHead;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_sensor_sonar_count)
    Button btnSonarCount;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_sensor_cliff_count)
    Button btnCliffCount;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_sensor_bumper_count)
    Button btnBumperCount;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_sensor_sonar_status)
    Button btnSonarStatus;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_sensor_cliff_status)
    Button btnCliffStatus;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.btn_sensor_bumper_status)
    Button btnBumperStatus;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_query_frequency_tips)
    TextView tvCurrentFrequency;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.sb_frequency)
    StripSeekBar sbFrequency;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.tv_query_all)
    TextView tvQueryAll;
    @SuppressLint("NonConstantResourceId")
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    /**
     * 最大设为2s，进度到200，按10倍的利率乘
     */
    private static final float FREQUENCY_MAX = 200.0f;
    private static final int FREQUENCY_MIN = 1;
    private static final int RATE = 10;
    private static final int TAG_SONAR_COUNT = 0;
    private static final int TAG_CLIFF_COUNT = 1;
    private static final int TAG_BUMPER_COUNT = 2;
    private static final int TAG_SONAR_STATUS = 3;
    private static final int TAG_CLIFF_STATUS = 4;
    private static final int TAG_BUMPER_STATUS = 5;
    /**
     * 默认频率
     */
    private static final int DEFAULT_FREQUENCY = 30;
    private static final int SPAN_COUNT = 3;
    private static final int TAG_NULL = -1;
    private static final int TAG_SONAR = 0;
    private static final int TAG_CLIFF = 1;
    private static final int TAG_BUMPER = 2;
    private static final int TAG_BATTERY = 3;
    private static final int MSG_SENSOR_COUNT = 1;
    private static final int MSG_SENSOR_STATUS = 2;
    private static final int MSG_SENSOR_INFO = 3;
    private SensorDetectAdapter mAdapter;
    private DetectThread mDetectThread;
    private MainHandler mMainHandler;
    private List<SetBean> mBeanList;
    private int mFrequency = DEFAULT_FREQUENCY;
    private boolean isStart, isSelectAll;
    private int mLastSelectId = TAG_NULL;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_sensor_info;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_sensor_info);
        String unKnown = getString(R.string.unknown);
        btnSonarCount.setText(getString(R.string.sensor_sonar, unKnown));
        btnCliffCount.setText(getString(R.string.sensor_cliff, unKnown));
        btnBumperCount.setText(getString(R.string.sensor_bumper, unKnown));
        btnSonarStatus.setText(getString(R.string.sensor_sonar, unKnown));
        btnCliffStatus.setText(getString(R.string.sensor_cliff, unKnown));
        btnBumperStatus.setText(getString(R.string.sensor_bumper, unKnown));
        tvCurrentFrequency.setText(getString(R.string.query_frequency_tips, mFrequency * RATE));
        sbFrequency.setProgress(mFrequency / FREQUENCY_MAX);
        sbFrequency.setOnSeekBarChangeListener(this);
        // 解决滑动冲突、滑动不流畅
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        int space = getResources().getDimensionPixelSize(R.dimen.item_split_size);
        recyclerView.addItemDecoration(new GridItemDecoration(space, space));
        recyclerView.setNestedScrollingEnabled(false);

        mAdapter = new SensorDetectAdapter(this, R.layout.recycler_item_sensor_detect);
        mAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(mAdapter);
        mBeanList = getDataList();
        mAdapter.setData(mBeanList);
        mMainHandler = new MainHandler(new WeakReference<>(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDetect();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onSeekBarStart(View view) {
    }

    @Override
    public void onProgressChange(View view, float progress) {
        setProgress(progress, false);
    }

    @Override
    public void onSeekBarStop(View view, float progress) {
        setProgress(progress, true);
        showToastTips(getString(R.string.set_success_tips));
    }

    @Override
    public void onItemClick(int position, SetBean data) {
        if (!SlamManager.getInstance().isConnected()) {
            showToastTips(getString(R.string.slam_not_connect_tips));
            return;
        }

        // 如果全选了就禁止点击
        if (isSelectAll) {
            return;
        }

        if (mAdapter != null && data != null) {
            int id = data.getId();
            // 如果已经选中再点击就取消
            if (id == mLastSelectId) {
                mLastSelectId = TAG_NULL;
                mAdapter.setSelect(TAG_NULL, false);
                stopDetect();
                return;
            }

            mAdapter.setSelect(position, false);
            startDetect(id, false);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @OnClick({R.id.btn_sensor_sonar_count, R.id.btn_sensor_cliff_count, R.id.btn_sensor_bumper_count, R.id.btn_sensor_sonar_status,
            R.id.btn_sensor_cliff_status, R.id.btn_sensor_bumper_status, R.id.tv_query_all})
    public void onClickView(View view) {
        if (!SlamManager.getInstance().isConnected()) {
            showToastTips(getString(R.string.slam_not_connect_tips));
            return;
        }

        switch (view.getId()) {
            case R.id.btn_sensor_sonar_count:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_SONAR_COUNT));
                break;
            case R.id.btn_sensor_cliff_count:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_CLIFF_COUNT));
                break;
            case R.id.btn_sensor_bumper_count:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_BUMPER_COUNT));
                break;
            case R.id.btn_sensor_sonar_status:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_SONAR_STATUS));
                break;
            case R.id.btn_sensor_cliff_status:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_CLIFF_STATUS));
                break;
            case R.id.btn_sensor_bumper_status:
                ThreadPoolManager.getInstance().execute(new TestRunnable(TAG_BUMPER_STATUS));
                break;
            case R.id.tv_query_all:
                queryAll();
                break;
            default:
                break;
        }
    }

    private List<SetBean> getDataList() {
        List<SetBean> titles = new ArrayList<>();
        String unknown = getString(R.string.unknown);
        SetBean bean = new SetBean(TAG_SONAR, getString(R.string.sensor_sonar, unknown));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_CLIFF);
        bean.setName(getString(R.string.sensor_cliff, unknown));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_BUMPER);
        bean.setName(getString(R.string.sensor_bumper, unknown));
        titles.add(bean);

        bean = bean.clone();
        bean.setId(TAG_BATTERY);
        bean.setName(getString(R.string.battery, unknown));
        titles.add(bean);
        return titles;
    }

    private void setSensorCount(int tag, int count) {
        showToastTips(getString(R.string.set_sensor_result, count >= 0));
        String content = count < 0 ? getString(R.string.unknown) : String.valueOf(count);
        switch (tag) {
            case TAG_SONAR_COUNT:
                btnSonarCount.setText(getString(R.string.sensor_sonar, content));
                break;
            case TAG_CLIFF_COUNT:
                btnCliffCount.setText(getString(R.string.sensor_cliff, content));
                break;
            case TAG_BUMPER_COUNT:
                btnBumperCount.setText(getString(R.string.sensor_bumper, content));
                break;
            default:
                break;
        }
    }

    private void setSensorStatus(int tag, List<Integer> list) {
        boolean isSuccess = list != null && !list.isEmpty();
        showToastTips(getString(R.string.set_sensor_result, isSuccess));
        String content;
        if (isSuccess) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0, size = list.size(); i < size; i++) {
                // 传感器id从1开始
                builder.append(i + 1);
                builder.append(BaseConstant.SENSOR_STATUS_SPLIT);
                builder.append(getSensorReason(list.get(i)));
                if (i != size - 1) {
                    builder.append(BaseConstant.SPLIT);
                }
            }

            content = builder.toString().trim();
        } else {
            content = getString(R.string.unknown);
        }

        switch (tag) {
            case TAG_SONAR_STATUS:
                btnSonarStatus.setText(getString(R.string.sensor_sonar, content));
                break;
            case TAG_CLIFF_STATUS:
                btnCliffStatus.setText(getString(R.string.sensor_cliff, content));
                break;
            case TAG_BUMPER_STATUS:
                btnBumperStatus.setText(getString(R.string.sensor_bumper, content));
                break;
            default:
                break;
        }
    }

    private String getSensorReason(int code) {
        // 状态分2种：0x01表示正常，0x02表示异常，其它值代表无效
        int errorCode = 1;
        if (code == errorCode) {
            return getString(R.string.open);
        }

        int exceptionCode = 2;
        if (code == exceptionCode) {
            return getString(R.string.error);
        }

        return getString(R.string.invalid);
    }

    private void queryAll() {
        isSelectAll = !tvQueryAll.isSelected();
        tvQueryAll.setSelected(isSelectAll);
        // 设置全选
        if (mAdapter != null) {
            mAdapter.setSelect(TAG_NULL, isSelectAll);
        }

        if (isSelectAll) {
            startDetect(TAG_NULL, true);
            return;
        }

        stopDetect();
    }

    private void startDetect(int id, boolean isSelectAll) {
        mLastSelectId = id;
        if (mDetectThread != null) {
            mDetectThread.setSelectId(id);
            mDetectThread.setSelectAll(isSelectAll);
            return;
        }

        isStart = true;
        mDetectThread = new DetectThread();
        mDetectThread.setSelectId(id);
        mDetectThread.setSelectAll(isSelectAll);
        mDetectThread.start();
    }

    private void stopDetect() {
        isStart = false;
        if (mDetectThread != null) {
            mDetectThread.interrupt();
            mDetectThread = null;
        }
    }

    private void setProgress(float progress, boolean isStop) {
        int frequency = (int) (progress * FREQUENCY_MAX);
        if (frequency < FREQUENCY_MIN) {
            frequency = FREQUENCY_MIN;
            sbFrequency.setProgress(frequency / FREQUENCY_MAX);
        }
        tvCurrentFrequency.setText(getString(R.string.query_frequency_tips, frequency * RATE));
        if (isStop) {
            mFrequency = frequency;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setContent(int id, String content) {
        if (mAdapter != null && mBeanList != null) {
            SetBean bean = mBeanList.get(id);
            bean.setName(content);
            mAdapter.notifyDataSetChanged();
        }
    }

    private class TestRunnable implements Runnable {
        private final int mTag;

        private TestRunnable(int tag) {
            mTag = tag;
        }

        @Override
        public void run() {
            if (mMainHandler == null) {
                return;
            }

            switch (mTag) {
                case TAG_SONAR_COUNT:
                    mMainHandler.obtainMessage(MSG_SENSOR_COUNT, mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.SONAR)).sendToTarget();
                    break;
                case TAG_CLIFF_COUNT:
                    mMainHandler.obtainMessage(MSG_SENSOR_COUNT, mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.CLIFF)).sendToTarget();
                    break;
                case TAG_BUMPER_COUNT:
                    mMainHandler.obtainMessage(MSG_SENSOR_COUNT, mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.BUMPER)).sendToTarget();
                    break;
                case TAG_SONAR_STATUS:
                    mMainHandler.obtainMessage(MSG_SENSOR_STATUS, mTag, 0, SlamManager.getInstance().getDefinedSensorStatus(IdType.SONAR)).sendToTarget();
                    break;
                case TAG_CLIFF_STATUS:
                    mMainHandler.obtainMessage(MSG_SENSOR_STATUS, mTag, 0, SlamManager.getInstance().getDefinedSensorStatus(IdType.CLIFF)).sendToTarget();
                    break;
                case TAG_BUMPER_STATUS:
                    mMainHandler.obtainMessage(MSG_SENSOR_STATUS, mTag, 0, SlamManager.getInstance().getDefinedSensorStatus(IdType.BUMPER)).sendToTarget();
                    break;
                default:
                    break;
            }
        }
    }

    private class DetectThread extends Thread {
        private boolean isSelectAll;
        private int mSelectId;

        @Override
        public void run() {
            super.run();
            while (isStart) {
                try {
                    if (mMainHandler == null) {
                        return;
                    }

                    List<Integer> list;
                    if (mSelectId == TAG_SONAR || isSelectAll) {
                        list = SlamManager.getInstance().getDefinedSensorDetectInfo(IdType.SONAR);
                        mMainHandler.obtainMessage(MSG_SENSOR_INFO, TAG_SONAR, 0, getContent(IdType.SONAR, list)).sendToTarget();
                        if (isSelectAll && isStart) {
                            Thread.sleep(getDelayTime());
                        }
                    }

                    if (!isStart) {
                        return;
                    }

                    if (mSelectId == TAG_CLIFF || isSelectAll) {
                        list = SlamManager.getInstance().getDefinedSensorDetectInfo(IdType.CLIFF);
                        mMainHandler.obtainMessage(MSG_SENSOR_INFO, TAG_CLIFF, 0, getContent(IdType.CLIFF, list)).sendToTarget();
                        if (isSelectAll && isStart) {
                            Thread.sleep(getDelayTime());
                        }
                    }

                    if (!isStart) {
                        return;
                    }

                    if (mSelectId == TAG_BUMPER || isSelectAll) {
                        list = SlamManager.getInstance().getDefinedSensorDetectInfo(IdType.BUMPER);
                        mMainHandler.obtainMessage(MSG_SENSOR_INFO, TAG_BUMPER, 0, getContent(IdType.BUMPER, list)).sendToTarget();
                        if (isSelectAll && isStart) {
                            Thread.sleep(getDelayTime());
                        }
                    }

                    if (!isStart) {
                        return;
                    }

                    if (mSelectId == TAG_BATTERY || isSelectAll) {
                        list = SlamManager.getInstance().getDefinedSensorDetectInfo(IdType.BATTERY);
                        mMainHandler.obtainMessage(MSG_SENSOR_INFO, TAG_BATTERY, 0, getContent(IdType.BATTERY, list)).sendToTarget();
                    }

                    if (isStart) {
                        Thread.sleep(getDelayTime());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }

        private void setSelectId(int id) {
            mSelectId = id;
        }

        private void setSelectAll(boolean isSelectAll) {
            this.isSelectAll = isSelectAll;
        }

        private long getDelayTime() {
            return (long) mFrequency * RATE;
        }

        private String getContent(IdType type, List<Integer> list) {
            String content = "";
            switch (type) {
                case SONAR:
                    content = getString(R.string.sensor_sonar, getSuffixTips(list, "cm"));
                    break;
                case CLIFF:
                    content = getString(R.string.sensor_cliff, getTriggerTips(list));
                    break;
                case BUMPER:
                    content = getString(R.string.sensor_bumper, getTriggerTips(list));
                    break;
                case BATTERY:
                    content = getString(R.string.battery, getSuffixTips(list, "%"));
                    break;
                default:
                    break;
            }

            return content;
        }

        private String getSuffixTips(List<Integer> list, String suffix) {
            if (list == null || list.isEmpty()) {
                return getString(R.string.unknown);
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0, size = list.size(); i < size; i++) {
                if (size > 1) {
                    // 传感器id从1开始
                    builder.append(i + 1);
                    builder.append(BaseConstant.SENSOR_STATUS_SPLIT);
                }

                builder.append(list.get(i));
                if (!TextUtils.isEmpty(suffix)) {
                    builder.append(suffix);
                }

                if (i != size - 1) {
                    builder.append(BaseConstant.SPLIT);
                }
            }

            return builder.toString().trim();
        }

        private String getTriggerTips(List<Integer> list) {
            if (list == null || list.isEmpty()) {
                return getString(R.string.unknown);
            }

            StringBuilder builder = new StringBuilder();
            for (int i = 0, size = list.size(); i < size; i++) {
                // 传感器id从1开始
                builder.append(i + 1);
                builder.append(BaseConstant.SENSOR_STATUS_SPLIT);
                builder.append(getString(list.get(i) == 1 ? R.string.trigger_true : R.string.trigger_false));
                if (i != size - 1) {
                    builder.append(BaseConstant.SPLIT);
                }
            }

            return builder.toString().trim();
        }
    }

    private static class MainHandler extends Handler {
        private final SensorInfoActivity mActivity;

        private MainHandler(WeakReference<SensorInfoActivity> reference) {
            super(Looper.getMainLooper());
            mActivity = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mActivity == null) {
                return;
            }

            switch (msg.what) {
                case MSG_SENSOR_COUNT:
                    mActivity.setSensorCount(msg.arg1, msg.arg2);
                    break;
                case MSG_SENSOR_STATUS:
                    mActivity.setSensorStatus(msg.arg1, (List<Integer>) msg.obj);
                    break;
                case MSG_SENSOR_INFO:
                    mActivity.setContent(msg.arg1, (String) msg.obj);
                    break;
                default:
                    break;
            }
        }
    }
}
