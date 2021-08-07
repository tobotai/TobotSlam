package com.tobot.map.module.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * 解决与嵌套控件横向滑动的冲突
 *
 * @author houdeming
 * @date 2020/12/31
 */
public class HorizontalConflictScrollView extends ScrollView {
    private float xDistance, yDistance, xLast, yLast;

    public HorizontalConflictScrollView(Context context) {
        this(context, null);
    }

    public HorizontalConflictScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalConflictScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xDistance = yDistance = 0f;
                xLast = ev.getX();
                yLast = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float curX = ev.getX();
                final float curY = ev.getY();
                xDistance += Math.abs(curX - xLast);
                yDistance += Math.abs(curY - yLast);
                xLast = curX;
                yLast = curY;
                // X轴滑动距离大于Y轴滑动距离，也就是用户横向滑动时，返回false，ScrollView不处理这次事件
                if (xDistance > yDistance) {
                    return false;
                }
                break;
            default:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
