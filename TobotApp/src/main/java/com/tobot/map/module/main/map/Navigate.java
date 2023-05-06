package com.tobot.map.module.main.map;

import android.content.Context;

import com.tobot.map.module.main.AbstractMove;
import com.tobot.map.module.main.MainActivity;
import com.tobot.slam.data.LocationBean;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
public class Navigate extends AbstractMove {

    public Navigate(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
        super(contextWeakReference, activityWeakReference);
    }

    @Override
    public void onNavigateResult(boolean isNavigateSuccess) {
        super.onNavigateResult(isNavigateSuccess);
        if (!isNavigateSuccess) {
            handleMoveFail(true);
        }
    }

    public void moveTo(float x, float y, float yaw) {
        LocationBean bean = new LocationBean();
        bean.setX(x);
        bean.setY(y);
        bean.setYaw(yaw);
        moveTo(bean);
    }
}
