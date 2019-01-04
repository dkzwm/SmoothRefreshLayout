package me.dkzwm.widget.srl;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;

/**
 * @author dkzwm
 */
public class ClassicSmoothRefreshLayout extends SmoothRefreshLayout {
    private ClassicHeader mClassicHeader;
    private ClassicFooter mClassicFooter;

    public ClassicSmoothRefreshLayout(Context context) {
        super(context);
    }

    public ClassicSmoothRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClassicSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ClassicSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.init(context, attrs, defStyleAttr, defStyleRes);
        mClassicHeader = new ClassicHeader(context);
        setHeaderView(mClassicHeader);
        mClassicFooter = new ClassicFooter(context);
        setFooterView(mClassicFooter);
    }

    public void setLastUpdateTimeKey(String key) {
        setLastUpdateTimeHeaderKey(key + "_header");
        setLastUpdateTimeFooterKey(key + "_footer");
    }

    public void setLastUpdateTimeHeaderKey(String key) {
        if (mClassicHeader != null) {
            mClassicHeader.setLastUpdateTimeKey(key);
        }
    }

    public void setLastUpdateTimeFooterKey(String key) {
        if (mClassicFooter != null) {
            mClassicFooter.setLastUpdateTimeKey(key);
        }
    }
}
