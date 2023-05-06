package com.tobot.map.module.set.firmware;

import android.content.Context;

import com.tobot.map.constant.BaseConstant;

/**
 * @author houdeming
 * @date 2022/09/14
 */
public class SlamModuleFirmwareFragment extends AbstractFirmwareFragment {

    public static SlamModuleFirmwareFragment newInstance() {
        return new SlamModuleFirmwareFragment();
    }

    @Override
    protected String getFirmwareDirectory(Context context) {
        return BaseConstant.getSlamFirmwareDirectory(context);
    }

    @Override
    protected void handleUpgrade(String filePath) {
    }
}
