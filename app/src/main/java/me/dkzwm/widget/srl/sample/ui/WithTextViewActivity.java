package me.dkzwm.widget.srl.sample.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import java.util.Random;

import me.dkzwm.widget.srl.IChangeStateAnimatorCreator;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithTextViewActivity extends AppCompatActivity implements View.OnClickListener {
    private SmoothRefreshLayout mRefreshLayout;
    private TextView mTextView;
    private Handler mHandler = new Handler();
    private Random mRandom = new Random();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_textview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_textView);
        mRefreshLayout = (SmoothRefreshLayout) findViewById(R.id.smoothRefreshLayout_with_textView);
        mTextView = (TextView) findViewById(R.id.textView_with_textView_desc);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setDisablePerformLoadMore(true);
        mRefreshLayout.setEnableHideFooterView(true);
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(boolean isRefresh) {
                mCount++;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.setState(SmoothRefreshLayout.STATE_CONTENT, true);
                        mRefreshLayout.refreshComplete();
                        String times = getString(R.string.number_of_refresh) + mCount;
                        mTextView.setText(times);
                    }
                }, 2000);
            }
        });
        mRefreshLayout.autoRefresh(true);
        mRefreshLayout.setChangeStateAnimatorCreator(new IChangeStateAnimatorCreator() {
            @NonNull
            @Override
            public ValueAnimator create(final View previous, final View current) {
                return randomAnimator(previous, current);
            }
        });
        findViewById(R.id.button_with_textView_change_empty_state)
                .setOnClickListener(this);
        findViewById(R.id.button_with_textView_change_content_state)
                .setOnClickListener(this);
        findViewById(R.id.button_with_textView_change_error_state)
                .setOnClickListener(this);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(WithTextViewActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_with_textView_change_empty_state:
                mRefreshLayout.setState(SmoothRefreshLayout.STATE_EMPTY, true);
                break;
            case R.id.button_with_textView_change_content_state:
                mRefreshLayout.setState(SmoothRefreshLayout.STATE_CONTENT, true);
                break;
            case R.id.button_with_textView_change_error_state:
                mRefreshLayout.setState(SmoothRefreshLayout.STATE_ERROR, true);
                break;
        }
    }

    private ValueAnimator randomAnimator(final View previous, final View current) {
        int randomInt = mRandom.nextInt(2);
        ValueAnimator animator;
        if (randomInt == 1) {
            animator = ObjectAnimator.ofFloat(1.0f, 0.0f).setDuration(500L);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    current.setAlpha(0);
                    current.setScaleY(0);
                    current.setScaleX(0);
                    current.setVisibility(View.VISIBLE);
                    previous.setAlpha(1);
                    previous.setScaleY(1);
                    previous.setScaleX(1);
                    previous.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    previous.setVisibility(View.GONE);
                    previous.setAlpha(1);
                    previous.setScaleY(1);
                    previous.setScaleX(1);
                    current.setAlpha(1);
                    current.setScaleX(1);
                    current.setScaleY(1);
                    current.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    previous.setVisibility(View.GONE);
                    previous.setAlpha(1);
                    previous.setScaleX(1);
                    previous.setScaleY(1);
                    current.setAlpha(1);
                    current.setScaleX(1);
                    current.setScaleY(1);
                    current.setVisibility(View.VISIBLE);
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (value > .5f) {
                        previous.setAlpha((value - .5f) * 2);
                        previous.setScaleX((value - .5f) * 2);
                        previous.setScaleY((value - .5f) * 2);
                        current.setAlpha(0);
                    } else {
                        previous.setAlpha(0);
                        current.setAlpha((.5f - value) * 2);
                        current.setScaleX((.5f - value) * 2);
                        current.setScaleY((.5f - value) * 2);
                    }
                    mRefreshLayout.requestLayout();
                }
            });
        } else {
            animator = ObjectAnimator.ofFloat(mRefreshLayout.getWidth(), 0.0f)
                    .setDuration(500L);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    previous.setVisibility(View.VISIBLE);
                    previous.setTranslationX(0);
                    current.setTranslationX(-mRefreshLayout.getWidth());
                    current.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    previous.setVisibility(View.GONE);
                    previous.setTranslationX(0);
                    current.setVisibility(View.VISIBLE);
                    current.setTranslationX(0);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    previous.setVisibility(View.GONE);
                    previous.setTranslationX(0);
                    current.setVisibility(View.VISIBLE);
                    current.setTranslationX(0);
                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    previous.setTranslationX(mRefreshLayout.getWidth() - value);
                    current.setTranslationX(-value);
                    mRefreshLayout.requestLayout();
                }
            });
        }
        return animator;
    }
}
