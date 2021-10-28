package com.tobot.map.module.set;

import android.widget.TextView;

import com.tobot.map.R;
import com.tobot.map.base.BaseActivity;

import butterknife.BindView;

/**
 * @author houdeming
 * @date 2019/10/19
 */
public class ContactUsActivity extends BaseActivity {
    @BindView(R.id.tv_head)
    TextView tvHead;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_contact_us;
    }

    @Override
    protected void init() {
        tvHead.setText(R.string.tv_contact_us);
    }
}
