package me.dkzwm.widget.srl.sample.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.TwoLevelSmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.TwoLevelRefreshView;
import me.dkzwm.widget.srl.indicator.ITwoLevelIndicator;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/12.
 *
 * @author dkzwm
 */
public class CustomTwoLevelHeader extends FrameLayout
        implements TwoLevelRefreshView<ITwoLevelIndicator> {
    private static final byte STATUS_PULL_DOWN = 1;
    private static final byte STATUS_RELEASE_TO_REFRESH = 2;
    private static final byte STATUS_TWO_LEVEL_REFRESH_HINT = 4;
    private static final byte STATUS_TWO_LEVEL_RELEASE_TO_REFRESH = 5;
    protected TextView mTextViewTitle;
    private byte mStatus = STATUS_PULL_DOWN;

    public CustomTwoLevelHeader(Context context) {
        this(context, null);
    }

    public CustomTwoLevelHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTwoLevelHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View header =
                LayoutInflater.from(context).inflate(R.layout.layout_custom_two_level_header, this);
        mTextViewTitle = (TextView) header.findViewById(R.id.textView_two_level_header_title);
    }

    @Override
    public int getType() {
        return TYPE_HEADER;
    }

    @Override
    public int getStyle() {
        return STYLE_DEFAULT;
    }

    @Override
    public int getCustomHeight() {
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onReset(SmoothRefreshLayout frame) {
        mTextViewTitle.setVisibility(VISIBLE);
        if (frame.isEnabledPullToRefresh()) {
            mTextViewTitle.setText(
                    me.dkzwm.widget.srl.ext.classic.R.string.sr_pull_down_to_refresh);
        } else {
            mTextViewTitle.setText(me.dkzwm.widget.srl.ext.classic.R.string.sr_pull_down);
        }
    }

    @Override
    public void onRefreshPrepare(SmoothRefreshLayout frame) {
        mTextViewTitle.setVisibility(VISIBLE);
        if (frame.isEnabledPullToRefresh()) {
            mTextViewTitle.setText(
                    me.dkzwm.widget.srl.ext.classic.R.string.sr_pull_down_to_refresh);
        } else {
            mTextViewTitle.setText(me.dkzwm.widget.srl.ext.classic.R.string.sr_pull_down);
        }
    }

    @Override
    public void onFingerUp(SmoothRefreshLayout layout, ITwoLevelIndicator indicator) {}

    @Override
    public void onRefreshBegin(SmoothRefreshLayout frame, ITwoLevelIndicator indicator) {
        mTextViewTitle.setText(me.dkzwm.widget.srl.ext.classic.R.string.sr_refreshing);
    }

    @Override
    public void onRefreshComplete(SmoothRefreshLayout frame, boolean isSuccessful) {
        mTextViewTitle.setVisibility(VISIBLE);
        mTextViewTitle.setText(me.dkzwm.widget.srl.ext.classic.R.string.sr_refresh_complete);
    }

    @Override
    public void onRefreshPositionChanged(
            SmoothRefreshLayout layout, byte status, ITwoLevelIndicator indicator) {
        final int currentPos = indicator.getCurrentPos();
        if (layout instanceof TwoLevelSmoothRefreshLayout) {
            TwoLevelSmoothRefreshLayout refreshLayout = (TwoLevelSmoothRefreshLayout) layout;
            if (!refreshLayout.isDisabledTwoLevelRefresh()
                    && status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
                final int offSetToTwoLevelRefresh = indicator.getOffsetToTwoLevelRefresh();
                if (currentPos < offSetToTwoLevelRefresh && indicator.crossTwoLevelHintLine()) {
                    if (mStatus != STATUS_TWO_LEVEL_REFRESH_HINT) {
                        mTextViewTitle.setText(R.string.continue_pull_down_to_have_a_surprise);
                        mStatus = STATUS_TWO_LEVEL_REFRESH_HINT;
                    }
                    return;
                } else if (currentPos >= offSetToTwoLevelRefresh) {
                    if (mStatus != STATUS_TWO_LEVEL_RELEASE_TO_REFRESH) {
                        mStatus = STATUS_TWO_LEVEL_RELEASE_TO_REFRESH;
                        if (!layout.isEnabledPullToRefresh()) {
                            mTextViewTitle.setText(R.string.release_your_finger_to_get_surprise);
                        }
                    }
                    return;
                }
            }
        }
        final int mOffsetToRefresh = indicator.getOffsetToRefresh();
        if (currentPos < mOffsetToRefresh
                && mStatus != STATUS_PULL_DOWN
                && status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            mStatus = STATUS_PULL_DOWN;
            if (layout.isEnabledPullToRefresh()) {
                mTextViewTitle.setText(
                        me.dkzwm.widget.srl.ext.classic.R.string.sr_pull_down_to_refresh);
            } else {
                mTextViewTitle.setText(me.dkzwm.widget.srl.ext.classic.R.string.sr_pull_down);
            }
        } else if (currentPos > mOffsetToRefresh
                && mStatus != STATUS_RELEASE_TO_REFRESH
                && indicator.hasTouched()
                && status == SmoothRefreshLayout.SR_STATUS_PREPARE) {
            mStatus = STATUS_RELEASE_TO_REFRESH;
            if (!layout.isEnabledPullToRefresh()) {
                mTextViewTitle.setText(
                        me.dkzwm.widget.srl.ext.classic.R.string.sr_release_to_refresh);
            }
        }
    }

    @Override
    public void onPureScrollPositionChanged(
            SmoothRefreshLayout layout, byte status, ITwoLevelIndicator indicator) {
        if (indicator.hasJustLeftStartPosition()) mTextViewTitle.setVisibility(GONE);
    }

    @Override
    public void onTwoLevelRefreshBegin(
            TwoLevelSmoothRefreshLayout layout, ITwoLevelIndicator twoLevelIndicator) {
        mTextViewTitle.setText(R.string.welcome_to_secondary_menu);
    }
}
