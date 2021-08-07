package com.tobot.map.module.common;

import com.slamtec.slamware.robot.MoveOption;

/**
 * @author houdeming
 * @date 2020/4/29
 */
public class MoveData {
    /**
     * 自由导航（默认）
     */
    public static final int NAVIGATE_FREE = 0;
    /**
     * 轨道导航
     */
    public static final int NAVIGATE_TRACK = 1;
    /**
     * 轨道优先
     */
    public static final int NAVIGATE_TRACK_FIRST = 2;
    /**
     * 普通到点（默认）
     */
    public static final int MOTION_TO_POINT_ORDINARY = 0;
    /**
     * 精确到点
     */
    public static final int MOTION_TO_POINT_EXACT = 1;
    /**
     * 遇障绕行（默认）
     */
    public static final int MEET_OBSTACLE_AVOID = 0;
    /**
     * 遇障暂停
     */
    public static final int MEET_OBSTACLE_SUSPEND = 1;
    /**
     * 默认速度
     */
    public static final float DEFAULT_SPEED = 0.70f;
    /**
     * 默认旋转速度[0.05-2.0]
     */
    public static final float DEFAULT_ROTATE_SPEED = 1.0f;
    private int mNavigateMode = NAVIGATE_FREE;
    private int mMotionMode = MOTION_TO_POINT_ORDINARY;
    private int mObstacleMode = MEET_OBSTACLE_AVOID;

    private MoveData() {
    }

    private static class MoveDataHolder {
        private static final MoveData INSTANCE = new MoveData();
    }

    public static MoveData getInstance() {
        return MoveDataHolder.INSTANCE;
    }

    public void setNavigateMode(int navigateMode) {
        mNavigateMode = navigateMode;
    }

    public int getNavigateMode() {
        return mNavigateMode;
    }

    public void setMotionMode(int motionMode) {
        mMotionMode = motionMode;
    }

    public int getMotionMode() {
        return mMotionMode;
    }

    public void setObstacleMode(int obstacleMode) {
        mObstacleMode = obstacleMode;
    }

    public int getObstacleMode() {
        return mObstacleMode;
    }

    public MoveOption getMoveOption() {
        MoveOption option = new MoveOption();
        // 让机器人停下来的时候旋转，true：旋转，false：不旋转
        option.setWithYaw(false);
        // 机器人移动的时候精确到点
        option.setPrecise(mMotionMode == MOTION_TO_POINT_EXACT);
        // 为true时，当机器人规划路径失败后，机器人不进行旋转重新规划
        option.setReturnUnreachableDirectly(false);
        // 不追加直接替换
        option.setAppending(false);
        // 以路径规划的形式到达该点，可以保证最优路径
        option.setMilestone(true);
        if (mNavigateMode == NAVIGATE_TRACK) {
            // 是否走虚拟轨道
            option.setKeyPoints(true);
        } else if (mNavigateMode == NAVIGATE_TRACK_FIRST) {
            // 是否走虚拟轨道
            option.setKeyPoints(true);
            // 为true时,机器人走虚拟轨道时候，也会进行避障，避障后继续优先走虚拟轨道.(如果不走虚拟轨道,trackWithOA 设置为true,没有作用)
            option.setTrackWithOA(true);
        }
        return option;
    }
}
