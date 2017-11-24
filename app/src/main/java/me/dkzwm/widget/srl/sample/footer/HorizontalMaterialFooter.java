package me.dkzwm.widget.srl.sample.footer;

import android.content.Context;
import android.util.AttributeSet;

import me.dkzwm.widget.srl.extra.footer.MaterialFooter;

/**
 * Created by dkzwm on 2017/10/23.
 *
 * @author dkzwm
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
        int width;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {
            width = specSize;
        } else {
            width = getCustomHeight() + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, specSize);
            }
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
