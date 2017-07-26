package me.dkzwm.smoothrefreshlayout.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.List;

import me.dkzwm.smoothrefreshlayout.RefreshingListenerAdapter;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.footer.ClassicFooter;
import me.dkzwm.smoothrefreshlayout.extra.header.ClassicHeader;
import me.dkzwm.smoothrefreshlayout.indicator.IIndicator;
import me.dkzwm.smoothrefreshlayout.sample.R;
import me.dkzwm.smoothrefreshlayout.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.smoothrefreshlayout.sample.header.CustomQQActivityHeader;
import me.dkzwm.smoothrefreshlayout.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/20.
 *
 * @author dkzwm
 */

public class TestQQActivityStyleActivity extends AppCompatActivity implements RadioGroup
        .OnCheckedChangeListener {
    private SmoothRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private RadioGroup mRadioGroup;
    private RadioButton mRadioButtonNormal;
    private RadioButton mRadioButtonActivity;
    private int mCount = 0;
    private ClassicHeader mClassicHeader;
    private ClassicFooter mClassicFooter;
    private CustomQQActivityHeader mQQActivityHeader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_qq_activity_style);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_qq_activity_style);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView_test_QQ_activity_style_activity);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup_test_QQ_activity_style_container);
        mRadioButtonNormal = (RadioButton) findViewById(R.id.radioButton_test_QQ_activity_style_normal);
        mRadioButtonActivity = (RadioButton) findViewById(R.id.radioButton_test_QQ_activity_style_activity);
        mRadioGroup.setOnCheckedChangeListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerViewAdapter(getLayoutInflater());
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout = (SmoothRefreshLayout) findViewById(R.id.smoothRefreshLayout_test_QQ_activity_style_activity);
        mRefreshLayout.setMode(SmoothRefreshLayout.MODE_BOTH);
        mClassicHeader = new ClassicHeader(this);
        mClassicHeader.setLastUpdateTimeKey("header_last_update_time");
        mClassicFooter = new ClassicFooter(this);
        mClassicFooter.setLastUpdateTimeKey("footer_last_update_time");
        mRefreshLayout.setHeaderView(mClassicHeader);
        mRefreshLayout.setFooterView(mClassicFooter);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(final boolean isRefresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRefresh) {
                            mCount = 0;
                            List<String> list = DataUtil.createList(mCount, 20);
                            mCount += 20;
                            mAdapter.updateData(list);
                        } else {
                            List<String> list = DataUtil.createList(mCount, 20);
                            mCount += 20;
                            mAdapter.appendData(list);
                        }
                        mRefreshLayout.refreshComplete();
                    }
                }, 2000);
            }

        });
        mRefreshLayout.addOnUIPositionChangedListener(new SmoothRefreshLayout.OnUIPositionChangedListener() {
            @Override
            public void onChanged(byte status, IIndicator indicator) {
                if (mRefreshLayout.isInStartPosition()) {
                    mRadioGroup.setEnabled(true);
                    mRadioButtonNormal.setEnabled(true);
                    mRadioButtonActivity.setEnabled(true);
                } else {
                    mRadioGroup.setEnabled(false);
                    mRadioButtonNormal.setEnabled(false);
                    mRadioButtonActivity.setEnabled(false);
                }
            }
        });
        mRefreshLayout.autoRefresh(false);
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
        startActivity(new Intent(TestQQActivityStyleActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radioButton_test_QQ_activity_style_activity:
                setActivityStyle();
                break;
            case R.id.radioButton_test_QQ_activity_style_normal:
                setNormalStyle();
                break;
        }
    }


    private void setActivityStyle() {
        if (mQQActivityHeader == null) {
            mQQActivityHeader = new CustomQQActivityHeader(this);
        }
        SmoothRefreshLayout.LayoutParams layoutParams = new SmoothRefreshLayout.LayoutParams
                (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mQQActivityHeader.setLayoutParams(layoutParams);
        mRefreshLayout.setHeaderView(mQQActivityHeader);
        mRefreshLayout.setFooterView(mClassicFooter);
        mRefreshLayout.setEnableHeaderDrawerStyle(true);
        mRefreshLayout.setEnableFooterDrawerStyle(true);
        mRefreshLayout.setDisablePerformRefresh(true);
        mRefreshLayout.setDurationToCloseHeader(2500);
        mRefreshLayout.setRatioOfHeaderHeightToRefresh(.22f);
        mRefreshLayout.setCanMoveTheMaxRatioOfHeaderHeight(.55f);
        mRefreshLayout.requestLayout();
    }

    private void setNormalStyle() {
        mRefreshLayout.setDurationToCloseHeader(500);
        mRefreshLayout.setHeaderView(mClassicHeader);
        mRefreshLayout.setFooterView(mClassicFooter);
        mRefreshLayout.setEnableHeaderDrawerStyle(false);
        mRefreshLayout.setEnableFooterDrawerStyle(false);
        mRefreshLayout.setDisablePerformRefresh(false);
        mRefreshLayout.setRatioOfHeaderHeightToRefresh(IIndicator.DEFAULT_RATIO_OF_REFRESH_VIEW_HEIGHT_TO_REFRESH);
        mRefreshLayout.requestLayout();
    }


}
