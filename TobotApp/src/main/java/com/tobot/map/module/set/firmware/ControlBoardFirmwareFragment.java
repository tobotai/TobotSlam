package com.tobot.map.module.set.firmware;

import android.content.Context;

import com.tobot.map.constant.BaseConstant;
import com.tobot.slam.SlamManager;

/**
 * @author houdeming
 * @date 2022/09/14
 */
public class ControlBoardFirmwareFragment extends AbstractFirmwareFragment {

    public static ControlBoardFirmwareFragment newInstance() {
        return new ControlBoardFirmwareFragment();
    }

    @Override
    protected String getFirmwareDirectory(Context context) {
        return BaseConstant.getFirmwareDirectory(context);
    }

    @Override
    protected void handleUpgrade(String filePath) {
        showProgressDialog(0);
        SlamManager.getInstance().upgradeControlPanelAsync(filePath, this);
    }
}
