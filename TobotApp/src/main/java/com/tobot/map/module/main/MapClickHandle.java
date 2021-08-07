package com.tobot.map.module.main;

import android.graphics.Point;
import android.view.MotionEvent;

import com.slamtec.slamware.geometry.Line;
import com.slamtec.slamware.geometry.PointF;
import com.slamtec.slamware.robot.ArtifactUsage;
import com.tobot.map.constant.BaseConstant;
import com.tobot.map.module.log.Logger;
import com.tobot.map.module.main.edit.OnEditListener;
import com.tobot.slam.SlamManager;
import com.tobot.slam.view.MapView;

import java.lang.ref.WeakReference;

/**
 * @author houdeming
 * @date 2020/4/24
 */
public class MapClickHandle {
    private MainActivity mMainActivity;
    private MapView mMapView;
    private boolean isStart;
    private PointF mPointStart, mPointEnd;

    public MapClickHandle(WeakReference<MainActivity> activityWeakReference, WeakReference<MapView> mapViewWeakReference) {
        mMainActivity = activityWeakReference.get();
        mMapView = mapViewWeakReference.get();
    }

    public void handleMapClick(int editTyp, int option, MotionEvent event, boolean isHandleMove) {
        if (mMapView == null) {
            return;
        }

        float x = event.getX();
        float y = event.getY();
        switch (option) {
            case OnEditListener.OPTION_CLOSE:
                if (isHandleMove) {
                    PointF pointF = mMapView.widgetCoordinateToMapCoordinate(x, y);
                    mMapView.setClickTips(pointF);
                    if (pointF != null) {
                        mMainActivity.moveTo(pointF.getX(), pointF.getY(), 0);
                    }
                }
                break;
            case OnEditListener.OPTION_ADD:
                addLine(editTyp, mMapView.widgetCoordinateToMapCoordinate(x, y));
                break;
            case OnEditListener.OPTION_REMOVE:
                removeLine(editTyp, new Point((int) x, (int) y));
                break;
            case OnEditListener.OPTION_CLEAR:
                clearLines(editTyp);
                break;
            default:
                break;
        }
    }

    public void clearLines(int editTyp) {
        isStart = false;
        mMapView.clearLines(getArtifactUsage(editTyp));
        SlamManager.getInstance().clearLinesInThread(getArtifactUsage(editTyp), null);
    }

    public void addLine(int editTyp, PointF pointF) {
        if (pointF == null) {
            return;
        }

        if (isStart) {
            isStart = false;
            mPointEnd = pointF;
        } else {
            isStart = true;
            mPointStart = pointF;
        }

        mMapView.setLine(getArtifactUsage(editTyp), pointF);
        if (!isStart) {
            SlamManager.getInstance().addLineInThread(getArtifactUsage(editTyp), new Line(mPointStart, mPointEnd), null);
        }
    }

    private void removeLine(int editTyp, Point point) {
        isStart = false;
        int lineId = mMapView.removeLine(getArtifactUsage(editTyp), point);
        Logger.i(BaseConstant.TAG, "remove lineId=" + lineId);
        // id不为-1的话，则代表有虚拟墙
        if (lineId != -1) {
            SlamManager.getInstance().removeLineByIdInThread(getArtifactUsage(editTyp), lineId, null);
        }
    }

    private ArtifactUsage getArtifactUsage(int editTyp) {
        return editTyp == OnEditListener.TYPE_VIRTUAL_WALL ? ArtifactUsage.ArtifactUsageVirutalWall : ArtifactUsage.ArtifactUsageVirtualTrack;
    }
}
