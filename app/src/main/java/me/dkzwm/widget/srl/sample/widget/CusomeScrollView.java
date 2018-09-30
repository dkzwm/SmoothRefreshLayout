package me.dkzwm.widget.srl.sample.widget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class CusomeScrollView extends ScrollView {
    public CusomeScrollView(Context context) {
        super(context);
    }

    public CusomeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CusomeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CusomeScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        Log.d(getClass().getCanonicalName(),"------------------x:"+ev.getX()+"   y:"+ev.getY());
        return super.onTouchEvent(ev);
    }
}
