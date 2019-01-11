package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.header.CustomQQActivityHeader;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/20.
 *
 * @author dkzwm
 */
public class TestQQActivityStyleActivity extends AppCompatActivity
        implements RadioGroup.OnCheckedChangeListener {
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
        mRecyclerView = findViewById(R.id.recyclerView_test_qq_activity_style);
        mRadioGroup = findViewById(R.id.radioGroup_test_qq_activity_style_container);
        mRadioButtonNormal = findViewById(R.id.radioButton_test_qq_activity_style_normal);
        mRadioButtonActivity = findViewById(R.id.radioButton_test_qq_activity_style_activity);
        mRadioGroup.setOnCheckedChangeListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerViewAdapter(this, getLayoutInflater());
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_qq_activity_style);
        mClassicHeader = new ClassicHeader(this);
        mClassicHeader.setLastUpdateTimeKey("header_last_update_time");
        mClassicFooter = new ClassicFooter(this);
        mClassicFooter.setLastUpdateTimeKey("footer_last_update_time");
        mRefreshLayout.setHeaderView(mClassicHeader);
        mRefreshLayout.setFooterView(mClassicFooter);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 20);
                                        mCount = list.size();
                                        mAdapter.updateData(list);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }

                    @Override
                    public void onLoadingMore() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 20);
                                        mCount += list.size();
                                        mAdapter.appendData(list);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }
                });
        mRefreshLayout.addOnUIPositionChangedListener(
                new SmoothRefreshLayout.OnUIPositionChangedListener() {
                    @Override
                    public void onChanged(byte status, IIndicator indicator) {
                        if (mRefreshLayout.getIndicator().isAlreadyHere(IIndicator.START_POS)) {
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
            case R.id.radioButton_test_qq_activity_style_activity:
                setActivityStyle();
                break;
            case R.id.radioButton_test_qq_activity_style_normal:
                setNormalStyle();
                break;
        }
    }

    private void setActivityStyle() {
        if (mQQActivityHeader == null) {
            mQQActivityHeader = new CustomQQActivityHeader(this);
        }
        SmoothRefreshLayout.LayoutParams layoutParams =
                new SmoothRefreshLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mQQActivityHeader.setLayoutParams(layoutParams);
        mRefreshLayout.setHeaderView(mQQActivityHeader);
        mRefreshLayout.setFooterView(mClassicFooter);
        mRefreshLayout.setEnableHeaderDrawerStyle(true);
        mRefreshLayout.setEnableFooterDrawerStyle(true);
        mRefreshLayout.setDisablePerformRefresh(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setDurationToCloseHeader(300);
        mRefreshLayout.setRatioOfHeaderToRefresh(.22f);
        mRefreshLayout.setMaxMoveRatioOfHeader(.55f);
        mRefreshLayout.requestLayout();
    }

    private void setNormalStyle() {
        mRefreshLayout.setDurationToCloseHeader(300);
        mRefreshLayout.setHeaderView(mClassicHeader);
        mRefreshLayout.setFooterView(mClassicFooter);
        mRefreshLayout.setEnableHeaderDrawerStyle(false);
        mRefreshLayout.setEnableFooterDrawerStyle(false);
        mRefreshLayout.setDisablePerformRefresh(false);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setMaxMoveRatioOfHeader(0f);
        mRefreshLayout.setRatioOfHeaderToRefresh(IIndicator.DEFAULT_RATIO_TO_REFRESH);
        mRefreshLayout.requestLayout();
    }
}
