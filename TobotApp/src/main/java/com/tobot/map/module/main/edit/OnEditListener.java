package com.tobot.map.module.main.edit;

/**
 * @author houdeming
 * @date 2020/4/24
 */
public interface OnEditListener {
    int TYPE_VIRTUAL_WALL = 0;
    int TYPE_VIRTUAL_TRACK = 1;
    int TYPE_RUBBER = 2;

    /**
     * 地图编辑
     *
     * @param type
     */
    void onEditClick(int type);

    /**
     * 关闭编辑
     */
    void onEditClose();
}
