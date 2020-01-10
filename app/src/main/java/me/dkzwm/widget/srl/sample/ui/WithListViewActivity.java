package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.extra.header.ClassicHeader;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.ListViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithListViewActivity extends AppCompatActivity implements View.OnClickListener {
    private SmoothRefreshLayout mRefreshLayout;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;
    private ClassicFooter mClassicFooter;
    private ClassicHeader mClassicHeader;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_listview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_listView);
        mListView = findViewById(R.id.listView_with_list);
        mAdapter = new ListViewAdapter(this, getLayoutInflater());
        mListView.setAdapter(mAdapter);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_listView);
        mClassicHeader = findViewById(R.id.classicHeader_with_listView);
        mClassicHeader.setLastUpdateTimeKey("header_last_update_time");
        mClassicHeader.setTitleTextColor(Color.BLACK);
        mClassicHeader.setLastUpdateTextColor(Color.BLACK);
        mClassicFooter = findViewById(R.id.classicFooter_with_listView);
        mClassicFooter.setLastUpdateTimeKey("footer_last_update_time");
        mClassicFooter.setNoMoreDataRes(R.string.no_more_data_currently_click_to_reload);
        mClassicFooter.setNoMoreDataClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRefreshLayout.setEnableNoMoreData(false);
                        mRefreshLayout.forceLoadMore();
                    }
                });
        mClassicFooter.setTitleTextColor(Color.BLACK);
        mClassicFooter.setLastUpdateTextColor(Color.BLACK);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setEnableOldTouchHandling(false);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setEnableAutoLoadMore(true);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 30);
                                        mCount = list.size();
                                        mAdapter.updateData(list);
                                        mRefreshLayout.refreshComplete(1200);
                                    }
                                },
                                5000);
                    }

                    @Override
                    public void onLoadingMore() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCount >= 50) {
                                            mRefreshLayout.setEnableNoMoreDataAndNoSpringBack(true);
                                        }
                                        mRefreshLayout.refreshComplete(1200);
                                    }
                                },
                                5000);
                    }
                });
        mRefreshLayout.addOnStatusChangedListener(
                new SmoothRefreshLayout.OnStatusChangedListener() {
                    @Override
                    public void onStatusChanged(byte old, byte now) {
                        if (old == SmoothRefreshLayout.SR_STATUS_LOADING_MORE
                                && now == SmoothRefreshLayout.SR_STATUS_COMPLETE) {
                            if (mCount < 50) {
                                List<String> list = DataUtil.createList(mCount, 20);
                                mCount += list.size();
                                mAdapter.appendData(list);
                            }
                        }
                    }
                });
        mRefreshLayout.setRatioToKeep(1);
        mRefreshLayout.setRatioToRefresh(1);
        mRefreshLayout.setMaxMoveRatioOfFooter(1);
        mRefreshLayout.setDisableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.autoRefresh(false);
        findViewById(R.id.button_with_listView_disable_refresh).setOnClickListener(this);
        findViewById(R.id.button_with_listView_enable_refresh).setOnClickListener(this);
        findViewById(R.id.button_with_listView_disable_loadMore).setOnClickListener(this);
        findViewById(R.id.button_with_listView_enable_loadMore).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_with_listView_disable_refresh:
                mRefreshLayout.setDisableRefresh(true);
                break;
            case R.id.button_with_listView_enable_refresh:
                mRefreshLayout.setDisableRefresh(false);
                break;
            case R.id.button_with_listView_disable_loadMore:
                mRefreshLayout.setDisableLoadMore(true);
                break;
            case R.id.button_with_listView_enable_loadMore:
                mRefreshLayout.setDisableLoadMore(false);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case Menu.FIRST:
                mClassicHeader.setStyle(IRefreshView.STYLE_DEFAULT);
                mClassicFooter.setStyle(IRefreshView.STYLE_DEFAULT);
                return true;
            case Menu.FIRST + 1:
                mClassicHeader.setStyle(IRefreshView.STYLE_SCALE);
                mClassicFooter.setStyle(IRefreshView.STYLE_SCALE);
                return true;
            case Menu.FIRST + 2:
                mClassicHeader.setStyle(IRefreshView.STYLE_PIN);
                mClassicFooter.setStyle(IRefreshView.STYLE_PIN);
                return true;
            case Menu.FIRST + 3:
                mClassicHeader.setStyle(IRefreshView.STYLE_FOLLOW_SCALE);
                mClassicFooter.setStyle(IRefreshView.STYLE_FOLLOW_SCALE);
                return true;
            case Menu.FIRST + 4:
                mClassicHeader.setStyle(IRefreshView.STYLE_FOLLOW_PIN);
                mClassicFooter.setStyle(IRefreshView.STYLE_FOLLOW_PIN);
                return true;
            case Menu.FIRST + 5:
                mClassicHeader.setStyle(IRefreshView.STYLE_FOLLOW_CENTER);
                mClassicFooter.setStyle(IRefreshView.STYLE_FOLLOW_CENTER);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.change_style_to_style_default);
        menu.add(Menu.NONE, Menu.FIRST + 1, Menu.NONE, R.string.change_style_to_style_scale);
        menu.add(Menu.NONE, Menu.FIRST + 2, Menu.NONE, R.string.change_style_to_style_pin);
        menu.add(Menu.NONE, Menu.FIRST + 3, Menu.NONE, R.string.change_style_to_style_follow_scale);
        menu.add(Menu.NONE, Menu.FIRST + 4, Menu.NONE, R.string.change_style_to_style_follow_pin);
        menu.add(
                Menu.NONE, Menu.FIRST + 5, Menu.NONE, R.string.change_style_to_style_follow_center);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(WithListViewActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
