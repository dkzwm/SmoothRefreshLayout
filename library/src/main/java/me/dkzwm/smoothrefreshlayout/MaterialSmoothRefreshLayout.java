package me.dkzwm.smoothrefreshlayout;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import me.dkzwm.smoothrefreshlayout.extra.footer.MaterialFooter;
import me.dkzwm.smoothrefreshlayout.extra.header.MaterialHeader;
import me.dkzwm.smoothrefreshlayout.utils.PixelUtl;

/**
 * @author dkzwm
 */
public class MaterialSmoothRefreshLayout extends SmoothRefreshLayout {
    private MaterialHeader mMaterialHeader;
    private MaterialFooter mMaterialFooter;

    public MaterialSmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public MaterialSmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews();
    }

    private void initViews() {
        mMode = MODE_BOTH;
        mMaterialHeader = new MaterialHeader(getContext());
        mMaterialHeader.setColorSchemeColors(new int[]{Color.RED, Color.BLUE, Color
                .GREEN, Color.BLACK});
        mMaterialHeader.setPadding(0, PixelUtl.dp2px(getContext(), 25), 0,
                PixelUtl.dp2px(getContext(), 20));
        setHeaderView(mMaterialHeader);
        mMaterialFooter = new MaterialFooter(getContext());
        setFooterView(mMaterialFooter);

    }

    public void materialStyle() {
        setRatioOfFooterHeightToRefresh(.95f);
        setCanMoveTheMaxRatioOfFooterHeight(1f);
        setEnablePinContentView(true);
        setEnableKeepRefreshView(true);
        setEnablePinRefreshViewWhileLoading(true);
        setEnableNextPtrAtOnce(true);
        mMaterialHeader.doHookUIRefreshComplete(this);
    }

}
