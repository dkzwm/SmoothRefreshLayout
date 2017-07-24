package me.dkzwm.smoothrefreshlayout.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.GridView;

import java.util.List;

import me.dkzwm.smoothrefreshlayout.RefreshingListenerAdapter;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.extra.footer.ClassicFooter;
import me.dkzwm.smoothrefreshlayout.extra.header.ClassicHeader;
import me.dkzwm.smoothrefreshlayout.sample.R;
import me.dkzwm.smoothrefreshlayout.sample.adapter.ListViewAdapter;
import me.dkzwm.smoothrefreshlayout.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithGridViewActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private GridView mGridView;
    private ListViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_gridview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_gridView);
        mGridView = (GridView) findViewById(R.id.gridView_with_gridView_activity);
        mAdapter = new ListViewAdapter(getLayoutInflater());
        mGridView.setAdapter(mAdapter);
        mRefreshLayout = (SmoothRefreshLayout) findViewById(R.id.smoothRefreshLayout_with_gridView_activity);
        mRefreshLayout.setMode(SmoothRefreshLayout.MODE_BOTH);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(final boolean isRefresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isRefresh) {
                            mCount = 0;
                            List<String> list = DataUtil.createList(mCount,100);
                            mCount += 100;
                            mAdapter.updateData(list);
                        } else {
                            List<String> list = DataUtil.createList(mCount,100);
                            mCount += 100;
                            mAdapter.appendData(list);
                        }
                        mRefreshLayout.refreshComplete();
                    }
                }, 2000);
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
        startActivity(new Intent(WithGridViewActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
