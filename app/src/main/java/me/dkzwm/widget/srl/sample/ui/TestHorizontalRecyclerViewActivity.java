package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

import me.dkzwm.widget.srl.HorizontalSmoothRefreshLayout;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.extra.footer.MaterialFooter;
import me.dkzwm.widget.srl.extra.header.MaterialHeader;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.HorizontalRecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;
import me.dkzwm.widget.srl.utils.PixelUtl;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class TestHorizontalRecyclerViewActivity extends AppCompatActivity {
    private HorizontalSmoothRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private HorizontalRecyclerViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_horizontal_recyclerview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_horizontal_recyclerView);
        mRecyclerView = findViewById(R.id.recyclerView_with_horizontal_recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3, LinearLayoutManager
                .HORIZONTAL, false));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new HorizontalRecyclerViewAdapter(this, getLayoutInflater());
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_horizontal_recyclerView);
        MaterialHeader header = new MaterialHeader(this);
        header.setColorSchemeColors(new int[]{Color.RED, Color.BLUE, Color
                .GREEN, Color.BLACK});
        header.setPadding(PixelUtl.dp2px(this, 25), 0, PixelUtl.dp2px(this, 25), 0);
        mRefreshLayout.setHeaderView(header);
        MaterialFooter footer = new MaterialFooter(this);
        footer.setProgressBarColors(new int[]{Color.RED, Color.BLUE, Color
                .GREEN, Color.BLACK});
        mRefreshLayout.setFooterView(footer);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setEnableScrollToBottomAutoLoadMore(true);
        mRefreshLayout.setEnablePinContentView(true);
        mRefreshLayout.setEnablePinRefreshViewWhileLoading(true);
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(final boolean isRefresh) {
                if (!isRefresh) {
                    Toast.makeText(TestHorizontalRecyclerViewActivity.this,
                            R.string.has_been_triggered_to_load_more,
                            Toast.LENGTH_SHORT).show();
                }
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
                }, 3000);
            }

            @Override
            public void onRefreshComplete(boolean isSuccessful) {
                Toast.makeText(TestHorizontalRecyclerViewActivity.this, R.string.sr_refresh_complete,
                        Toast.LENGTH_SHORT).show();
            }
        });
        mRefreshLayout.setDurationToClose(800);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TestHorizontalRecyclerViewActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
