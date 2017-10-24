package me.dkzwm.widget.srl.indicator;

/**
 * Created by dkzwm on 2017/10/20.
 */

public class HorizontalDefaultIndicator extends DefaultIndicator {
    @Override
    public void onFingerMove(float x, float y) {
        mMoved = true;
        float offset = (x - mLastMovePoint[0]);
        processOnMove(offset);
        mLastMovePoint[0] = x;
        mLastMovePoint[1] = y;
    }

}
