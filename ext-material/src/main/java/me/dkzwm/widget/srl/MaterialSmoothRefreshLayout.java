package me.dkzwm.widget.srl;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import me.dkzwm.widget.srl.extra.footer.MaterialFooter;
import me.dkzwm.widget.srl.extra.header.MaterialHeader;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.utils.PixelUtl;

/**
 * @author dkzwm
 */
public class MaterialSmoothRefreshLayout extends SmoothRefreshLayout {
    private MaterialHeader mMaterialHeader;
    private MaterialFooter mMaterialFooter;
    private OnUIPositionChangedListener mOnUIPositionChangedListener = new OnUIPositionChangedListener() {
        int mLastMovingStatus = IIndicator.MOVING_CONTENT;

        @Override
        public void onChanged(byte status, IIndicator indicator) {
            int movingStatus = indicator.getMovingStatus();
            if (movingStatus == IIndicator.MOVING_HEADER) {
                if (movingStatus != mLastMovingStatus) {
                    setEnablePinContentView(true);
                    setEnablePinRefreshViewWhileLoading(true);
                }
            } else {
                if (movingStatus != mLastMovingStatus)
                    setEnablePinContentView(false);
            }
            mLastMovingStatus = movingStatus;
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
     * Quickly set to material style. Before you change the configuration, you must know which
     * parameters have been configured
     */
    public void materialStyle() {
        setRatioOfFooterHeightToRefresh(.95f);
        setCanMoveTheMaxRatioOfFooterHeight(1f);
        setCanMoveTheMaxRatioOfHeaderHeight(1.5f);
        setEnablePinContentView(true);
        setEnableKeepRefreshView(true);
        setEnablePinRefreshViewWhileLoading(true);
        setEnableNextPtrAtOnce(true);
        if (mHeaderView != null && mHeaderView instanceof MaterialHeader)
            ((MaterialHeader) mHeaderView).doHookUIRefreshComplete(this);
        if (!isDisabledLoadMore()) {
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
