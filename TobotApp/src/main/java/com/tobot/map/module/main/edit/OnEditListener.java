package com.tobot.map.module.main.edit;

import com.slamtec.slamware.geometry.PointF;

/**
 * @author houdeming
 * @date 2020/4/24
 */
public interface OnEditListener {
    int TYPE_VIRTUAL_WALL = 0;
    int TYPE_VIRTUAL_TRACK = 1;
    int TYPE_RUBBER = 2;
    int OPTION_CLOSE = 0;
    int OPTION_ADD = 1;
    int OPTION_REMOVE = 2;
    int OPTION_CLEAR = 3;
    int OPTION_WIPE_WHITE = 4;
    int OPTION_WIPE_GREY = 5;
    int OPTION_WIPE_BLACK = 6;
    int OPTION_WIPE_CANCEL = 7;

    /**
     * 地图编辑
     *
     * @param type
     */
    void onEditClick(int type);

    /**
     * 墙编辑
     *
     * @param option
     */
    void onEditOption(int option);

    /**
     * 添加线
     *
     * @param pointF
     */
    void onAddLine(PointF pointF);
}
