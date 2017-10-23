package me.dkzwm.widget.srl.extra.header;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * Created by dkzwm on 2017/10/23.
 */

public class HorizontalMaterialHeader extends MaterialHeader {
    public HorizontalMaterialHeader(Context context) {
        super(context);
    }

    public HorizontalMaterialHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalMaterialHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = mDrawable.getIntrinsicWidth() + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(width, MeasureSpec.getSize(heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int size = mDrawable.getIntrinsicHeight();
        mDrawable.setBounds(0, 0, size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLUE);
        final int saveCount = canvas.save();
        Rect rect = mDrawable.getBounds();
        int top = getPaddingTop() + (getMeasuredHeight() - mDrawable.getIntrinsicWidth()) / 2;
        canvas.translate(getPaddingLeft(), top);
        canvas.scale(mScale, mScale, rect.exactCenterX(), rect.exactCenterY());
        mDrawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }
}
