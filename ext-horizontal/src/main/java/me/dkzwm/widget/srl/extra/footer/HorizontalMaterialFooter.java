package me.dkzwm.widget.srl.extra.footer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;

/**
 * Created by dkzwm on 2017/10/23.
 */

public class HorizontalMaterialFooter extends MaterialFooter {
    public HorizontalMaterialFooter(Context context) {
        super(context);
    }

    public HorizontalMaterialFooter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalMaterialFooter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mStyle == STYLE_DEFAULT || mStyle == STYLE_FOLLOW_PIN || mStyle == STYLE_PIN
                || mStyle == STYLE_FOLLOW_CENTER) {
            setMeasuredDimension(mDefaultSize, MeasureSpec.getSize(heightMeasureSpec));
        } else if (mStyle == STYLE_FOLLOW_SCALE && !mHasLeftHeaderHeight) {
            setMeasuredDimension(getCustomHeight(), MeasureSpec.getSize(heightMeasureSpec));
        } else {
            setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
                    MeasureSpec.getSize(heightMeasureSpec));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        super.onDraw(canvas);
    }
}
