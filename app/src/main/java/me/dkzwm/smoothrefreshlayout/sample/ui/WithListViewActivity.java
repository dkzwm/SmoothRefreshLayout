package me.dkzwm.smoothrefreshlayout.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import me.dkzwm.smoothrefreshlayout.RefreshingListenerAdapter;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.footer.ClassicFooter;
import me.dkzwm.smoothrefreshlayout.extra.header.ClassicHeader;
import me.dkzwm.smoothrefreshlayout.sample.R;
import me.dkzwm.smoothrefreshlayout.sample.adapter.ListViewAdapter;
import me.dkzwm.smoothrefreshlayout.sample.util.DataUtil;
import me.dkzwm.smoothrefreshlayout.utils.ScrollCompat;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithListViewActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;
    private int mFailedCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_listview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_listView);
        mListView = (ListView) findViewById(R.id.listView_with_listView_activity);
        mAdapter = new ListViewAdapter(getLayoutInflater());
        mListView.setAdapter(mAdapter);
        mRefreshLayout = (SmoothRefreshLayout) findViewById(R.id.smoothRefreshLayout_with_listView_activity);
        mRefreshLayout.setMode(SmoothRefreshLayout.MODE_BOTH);
        final ClassicHeader header = new ClassicHeader(this);
        header.setLastUpdateTimeKey("header_last_update_time");
        final ClassicFooter footer = new ClassicFooter(this);
        footer.setLastUpdateTimeKey("footer_last_update_time");
        mRefreshLayout.setHeaderView(header);
        mRefreshLayout.setFooterView(footer);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setRatioOfFooterHeightToRefresh(0.001f);
        mRefreshLayout.setEnableWhenScrollingToBottomToPerformLoadMore(true);
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(final boolean isRefresh) {
                if (!isRefresh) {
                    Toast.makeText(WithListViewActivity.this, R.string.has_been_triggered_to_load_more,
                            Toast.LENGTH_SHORT).show();
                }
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFailedCount++;
                        if (mFailedCount % 2 == 0) {
                            mRefreshLayout.refreshComplete(false);
                            return;
                        }
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
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    if (!ScrollCompat.canChildScrollDown(view)) {
                        mRefreshLayout.autoLoadMore(true);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {

            }
        });
        //Hook刷新完成，可以实现延迟完成加载
        mRefreshLayout.setOnHookUIRefreshCompleteCallback(new SmoothRefreshLayout
                .OnHookUIRefreshCompleteCallBack() {
            @Override
            public void onHook(final SmoothRefreshLayout.RefreshCompleteHook hook) {
                if (mRefreshLayout.isRefreshing())
                    header.onRefreshComplete(mRefreshLayout);
                else
                    footer.onRefreshComplete(mRefreshLayout);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hook.onHookComplete();
                    }
                }, 500);
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
        startActivity(new Intent(WithListViewActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
