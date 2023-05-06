package com.tobot.map.base;

import android.view.View;

import com.tobot.map.R;

import butterknife.OnClick;

/**
 * @author houdeming
 * @date 2022/06/27
 */
public abstract class BaseBackActivity extends BaseActivity {

    @OnClick({R.id.tv_back})
    public void onBackClickView(View view) {
        if (view.getId() == R.id.tv_back) {
            finish();
        }
    }
}
