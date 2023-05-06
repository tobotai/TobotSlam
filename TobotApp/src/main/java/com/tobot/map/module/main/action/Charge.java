package com.tobot.map.module.main.action;

import android.content.Context;

import com.tobot.map.module.main.AbstractMove;
import com.tobot.map.module.main.MainActivity;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2019/10/23
 */
public class Charge extends AbstractMove {

    public Charge(WeakReference<Context> contextWeakReference, WeakReference<MainActivity> activityWeakReference) {
        super(contextWeakReference, activityWeakReference);
    }

    @Override
    public void onChargeResult(boolean isChargeSuccess) {
        super.onChargeResult(isChargeSuccess);
        if (!isChargeSuccess) {
            handleMoveFail(true);
        }
    }
}
