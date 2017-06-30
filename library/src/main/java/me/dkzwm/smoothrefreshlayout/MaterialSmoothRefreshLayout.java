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
        MaterialHeader header = new MaterialHeader(getContext());
        header.setColorSchemeColors(new int[]{Color.RED, Color.BLUE, Color
                .GREEN, Color.BLACK});
        header.setPadding(0, PixelUtl.dp2px(getContext(), 25), 0,
                PixelUtl.dp2px(getContext(), 20));
        setHeaderView(header);
        setFooterView(new MaterialFooter(getContext()));
    }

    /**
     * Set the header view padding
     *
     * @param top    The padding in dip unit from the top of this view
     * @param bottom The padding in dip unit from the top of this view
     */
    public void setHeaderViewPadding(int top, int bottom) {
        if (mHeaderView != null) {
            mHeaderView.getView().setPadding(0, PixelUtl.dp2px(getContext(), top),
                    0, PixelUtl.dp2px(getContext(), bottom));
            requestLayout();
        }
    }

    public void materialStyle() {
        setRatioOfFooterHeightToRefresh(.95f);
        setCanMoveTheMaxRatioOfFooterHeight(1f);
        setEnablePinContentView(true);
        setEnableKeepRefreshView(true);
        setEnablePinRefreshViewWhileLoading(true);
        setEnableNextPtrAtOnce(true);
        if (mHeaderView != null && mHeaderView instanceof MaterialHeader)
            ((MaterialHeader) mHeaderView).doHookUIRefreshComplete(this);
    }
}
