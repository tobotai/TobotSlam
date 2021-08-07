package com.tobot.map.module.set.firmware;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tobot.bar.base.BaseBar;
import com.tobot.bar.seekbar.StripSeekBar;
import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.base.BaseRecyclerAdapter;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.entity.SetBean;
import com.tobot.map.module.common.GridItemDecoration;
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
public class GetSensorDetectActivity extends BaseActivity implements BaseBar.OnSeekBarChangeListener, BaseRecyclerAdapter.OnItemClickListener<SetBean> {
    @BindView(R.id.tv_head)
    TextView tvHead;
    @BindView(R.id.tv_query_frequency_tips)
    TextView tvCurrentFrequency;
    @BindView(R.id.sb_frequency)
    StripSeekBar sbFrequency;
    @BindView(R.id.tv_query_all)
    TextView tvQueryAll;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private static final float FREQUENCY_MAX = 100.0f;
    private static final float RATE = 10.0f;
    /**
     * 默认频率
     */
    private static final int DEFAULT_FREQUENCY = 10;
    private static final int SPAN_COUNT = 3;
    private static final int TAG_NULL = -1;
    private static final int TAG_SONAR = 0;
    private static final int TAG_CLIFF = 1;
    private static final int TAG_BUMPER = 2;
    private static final int TAG_BATTERY = 3;
    private SensorDetectAdapter mAdapter;
    private DetectThread mDetectThread;
    private MainHandler mMainHandler;
    private List<SetBean> mBeanList;
    private int mFrequency = DEFAULT_FREQUENCY;
    private boolean isStart, isSelectAll;
    private int mLastSelectId = TAG_NULL;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_get_sensor_detect;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.get_sensor_detect_info);
        tvCurrentFrequency.setText(getString(R.string.query_frequency_tips, getString(R.string.float_1_format, mFrequency / RATE)));
        sbFrequency.setProgress(mFrequency / FREQUENCY_MAX);
        sbFrequency.setOnSeekBarChangeListener(this);

        recyclerView.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));
        int space = getResources().getDimensionPixelSize(R.dimen.item_split_size);
        recyclerView.addItemDecoration(new GridItemDecoration(space, space));

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

    @OnClick({R.id.tv_query_all})
    public void onClickView(View v) {
        if (v.getId() == R.id.tv_query_all) {
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
        if (frequency < DEFAULT_FREQUENCY) {
            frequency = DEFAULT_FREQUENCY;
            sbFrequency.setProgress(frequency / FREQUENCY_MAX);
        }
        tvCurrentFrequency.setText(getString(R.string.query_frequency_tips, getString(R.string.float_1_format, frequency / RATE)));
        if (isStop) {
            mFrequency = frequency;
        }
    }

    private void setContent(int id, String content) {
        if (mAdapter != null && mBeanList != null) {
            SetBean bean = mBeanList.get(id);
            bean.setName(content);
            mAdapter.notifyDataSetChanged();
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
                        mMainHandler.obtainMessage(TAG_SONAR, getContent(IdType.SONAR, list)).sendToTarget();
                        if (isSelectAll && isStart) {
                            Thread.sleep(getDelayTime());
                        }
                    }

                    if (!isStart) {
                        return;
                    }

                    if (mSelectId == TAG_CLIFF || isSelectAll) {
                        list = SlamManager.getInstance().getDefinedSensorDetectInfo(IdType.CLIFF);
                        mMainHandler.obtainMessage(TAG_CLIFF, getContent(IdType.CLIFF, list)).sendToTarget();
                        if (isSelectAll && isStart) {
                            Thread.sleep(getDelayTime());
                        }
                    }

                    if (!isStart) {
                        return;
                    }

                    if (mSelectId == TAG_BUMPER || isSelectAll) {
                        list = SlamManager.getInstance().getDefinedSensorDetectInfo(IdType.BUMPER);
                        mMainHandler.obtainMessage(TAG_BUMPER, getContent(IdType.BUMPER, list)).sendToTarget();
                        if (isSelectAll && isStart) {
                            Thread.sleep(getDelayTime());
                        }
                    }

                    if (!isStart) {
                        return;
                    }

                    if (mSelectId == TAG_BATTERY || isSelectAll) {
                        list = SlamManager.getInstance().getDefinedSensorDetectInfo(IdType.BATTERY);
                        mMainHandler.obtainMessage(TAG_BATTERY, getContent(IdType.BATTERY, list)).sendToTarget();
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
            return mFrequency * 100;
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
                    builder.append(i);
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
                if (size > 1) {
                    builder.append(i);
                    builder.append(BaseConstant.SENSOR_STATUS_SPLIT);
                }

                builder.append(getString(list.get(i) == 1 ? R.string.trigger_true : R.string.trigger_false));
                if (i != size - 1) {
                    builder.append(BaseConstant.SPLIT);
                }
            }

            return builder.toString().trim();
        }
    }

    private static class MainHandler extends Handler {
        private GetSensorDetectActivity mActivity;

        private MainHandler(WeakReference<GetSensorDetectActivity> reference) {
            super(Looper.getMainLooper());
            mActivity = reference.get();
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (mActivity != null) {
                mActivity.setContent(msg.what, (String) msg.obj);
            }
        }
    }
}
