package com.tobot.map.module.set.firmware;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.util.ThreadPoolManager;
import com.tobot.slam.SlamManager;
import com.tobot.slam.agent.firmware.IdType;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2021/08/26
 */
public class SensorInfoActivity extends BaseActivity {
    @BindView(R.id.tv_head)
    TextView tvHead;
    @BindView(R.id.btn_sensor_sonar_count)
    Button btnSonarCount;
    @BindView(R.id.btn_sensor_cliff_count)
    Button btnCliffCount;
    @BindView(R.id.btn_sensor_bumper_count)
    Button btnBumperCount;
    @BindView(R.id.btn_sensor_sonar_status)
    Button btnSonarStatus;
    @BindView(R.id.btn_sensor_cliff_status)
    Button btnCliffStatus;
    @BindView(R.id.btn_sensor_bumper_status)
    Button btnBumperStatus;
    private static final int TAG_SONAR_COUNT = 0;
    private static final int TAG_CLIFF_COUNT = 1;
    private static final int TAG_BUMPER_COUNT = 2;
    private static final int TAG_SONAR_STATUS = 3;
    private static final int TAG_CLIFF_STATUS = 4;
    private static final int TAG_BUMPER_STATUS = 5;

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
    }

    @OnClick({R.id.btn_sensor_sonar_count, R.id.btn_sensor_cliff_count, R.id.btn_sensor_bumper_count,
            R.id.btn_sensor_sonar_status, R.id.btn_sensor_cliff_status, R.id.btn_sensor_bumper_status})
    public void onClickView(View view) {
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
            default:
                break;
        }
    }

    private void setSensorCount(int tag, int count) {
        showToastTips(getString(R.string.set_sensor_result, count >= 0));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    private void setSensorStatus(int tag, List<Integer> list) {
        boolean isSuccess = list != null && !list.isEmpty();
        showToastTips(getString(R.string.set_sensor_result, isSuccess));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String content;
                if (isSuccess) {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0, size = list.size(); i < size; i++) {
                        if (size > 1) {
                            builder.append(i);
                            builder.append(BaseConstant.SENSOR_STATUS_SPLIT);
                        }

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
        });
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

    private class TestRunnable implements Runnable {
        private int mTag;

        private TestRunnable(int tag) {
            mTag = tag;
        }

        @Override
        public void run() {
            switch (mTag) {
                case TAG_SONAR_COUNT:
                    setSensorCount(mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.SONAR));
                    break;
                case TAG_CLIFF_COUNT:
                    setSensorCount(mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.CLIFF));
                    break;
                case TAG_BUMPER_COUNT:
                    setSensorCount(mTag, SlamManager.getInstance().getDefinedSensorCount(IdType.BUMPER));
                    break;
                case TAG_SONAR_STATUS:
                    setSensorStatus(mTag, SlamManager.getInstance().getDefinedSensorStatus(IdType.SONAR));
                    break;
                case TAG_CLIFF_STATUS:
                    setSensorStatus(mTag, SlamManager.getInstance().getDefinedSensorStatus(IdType.CLIFF));
                    break;
                case TAG_BUMPER_STATUS:
                    setSensorStatus(mTag, SlamManager.getInstance().getDefinedSensorStatus(IdType.BUMPER));
                    break;
                default:
                    break;
            }
        }
    }
}
