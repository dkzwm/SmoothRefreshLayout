package me.dkzwm.smoothrefreshlayout;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import me.dkzwm.smoothrefreshlayout.extra.footer.MaterialFooter;
import me.dkzwm.smoothrefreshlayout.extra.header.MaterialHeader;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.utils.PixelUtl;

/**
 * @author dkzwm
 */
public class MaterialSmoothRefreshLayout extends SmoothRefreshLayout {
    private MaterialHeader mMaterialHeader;
    private MaterialFooter mMaterialFooter;
    private OnUIPositionChangedListener mOnUIPositionChangedListener = new OnUIPositionChangedListener() {
        @Override
        public void onChanged(byte status, IIndicator indicator) {
            if (indicator.getMovingStatus() == IIndicator.MOVING_FOOTER) {
                setEnablePinContentView(false);
            } else {
                setEnablePinContentView(true);
                setEnablePinRefreshViewWhileLoading(true);
            }
        }
    };

    public MaterialSmoothRefreshLayout(Context context) {
        this(context, null);
    }

    public MaterialSmoothRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaterialSmoothRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initViews(context);
    }

    private void initViews(Context context) {
        mMode = MODE_BOTH;
        mMaterialHeader = new MaterialHeader(context);
        mMaterialHeader.setColorSchemeColors(new int[]{Color.RED, Color.BLUE, Color
                .GREEN, Color.BLACK});
        mMaterialHeader.setPadding(0, PixelUtl.dp2px(context, 25), 0,
                PixelUtl.dp2px(context, 20));
        setHeaderView(mMaterialHeader);
        mMaterialFooter = new MaterialFooter(context);
        setFooterView(mMaterialFooter);
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

    /**
     * Quickly set to material style. Before you change the configuration, you must know which
     * parameters have been configured
     */
    public void materialStyle() {
        setRatioOfFooterHeightToRefresh(.95f);
        setCanMoveTheMaxRatioOfFooterHeight(1f);
        setEnablePinContentView(true);
        setEnableKeepRefreshView(true);
        setEnablePinRefreshViewWhileLoading(true);
        setEnableNextPtrAtOnce(true);
        if (mHeaderView != null && mHeaderView instanceof MaterialHeader)
            ((MaterialHeader) mHeaderView).doHookUIRefreshComplete(this);
        if (mMode == MODE_BOTH || mMode == MODE_LOAD_MORE) {
            removeOnUIPositionChangedListener(mOnUIPositionChangedListener);
            addOnUIPositionChangedListener(mOnUIPositionChangedListener);
        }
    }

    public MaterialHeader getDefaultHeader() {
        return mMaterialHeader;
    }

    public MaterialFooter getDefaultFooter() {
        return mMaterialFooter;
    }

    public OnUIPositionChangedListener getDefaultOnUIPositionChangedListener() {
        return mOnUIPositionChangedListener;
    }
}
