package me.dkzwm.smoothrefreshlayout;

import android.content.Context;
import android.util.AttributeSet;

import me.dkzwm.smoothrefreshlayout.extra.footer.ClassicFooter;
import me.dkzwm.smoothrefreshlayout.extra.header.ClassicHeader;

public class ClassicSmoothRefreshLayout extends SmoothRefreshLayout {
    private ClassicHeader mClassicHeader;
    private ClassicFooter mClassicFooter;

    public ClassicSmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public ClassicSmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews() {
        mMode = MODE_BOTH;
        mClassicHeader = new ClassicHeader(getContext());
        setHeaderView(mClassicHeader);
        mClassicFooter = new ClassicFooter(getContext());
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
