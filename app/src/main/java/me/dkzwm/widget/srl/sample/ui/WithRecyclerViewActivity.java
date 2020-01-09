package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import me.dkzwm.widget.srl.MaterialSmoothRefreshLayout;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.extra.footer.MaterialFooter;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithRecyclerViewActivity extends AppCompatActivity {
    private MaterialSmoothRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_recyclerview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_recyclerView);
        mRecyclerView = findViewById(R.id.recyclerView_with_recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerViewAdapter(this, getLayoutInflater());
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_recyclerView);
        mRefreshLayout.setDisableLoadMore(false);
        //        mRefreshLayout.materialStyle();
        mRefreshLayout.setEnableAutoLoadMore(true);
        mRefreshLayout.setEnableAutoRefresh(true);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 60);
                                        mCount = list.size();
                                        mAdapter.insertData(list);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }

                    @Override
                    public void onLoadingMore() {
                        Toast.makeText(
                                        WithRecyclerViewActivity.this,
                                        R.string.has_been_triggered_to_load_more,
                                        Toast.LENGTH_SHORT)
                                .show();
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 15);
                                        mCount += list.size();
                                        mAdapter.appendData(list);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }
                });
        mRefreshLayout.setDisableLoadMoreWhenContentNotFull(true);
        mRefreshLayout.autoRefresh(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case Menu.FIRST:
                if (mRefreshLayout.getFooterView().getStyle() == IRefreshView.STYLE_SCALE)
                    ((MaterialFooter) mRefreshLayout.getFooterView())
                            .setStyle(IRefreshView.STYLE_DEFAULT);
                else
                    ((MaterialFooter) mRefreshLayout.getFooterView())
                            .setStyle(IRefreshView.STYLE_SCALE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.change_style);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(WithRecyclerViewActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
