package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.List;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class TestRecyclerViewInNestedScrollViewInSrlActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private Handler mHandler = new Handler();
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private int mCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_recyclerview_in_nestedscrollview_in_srl);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white_72x72);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
        mRecyclerView = findViewById(R.id.recyclerView_test_recyclerView_in_nestedScrollView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mAdapter = new RecyclerViewAdapter(this, getLayoutInflater());
        mRecyclerView.setAdapter(mAdapter);
        mRefreshLayout =
                findViewById(R.id.smoothRefreshLayout_test_recyclerView_in_nestedScrollView);
        mRefreshLayout.setDisableLoadMore(false);
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
                                        mAdapter.updateData(list);
                                        mRefreshLayout.refreshComplete(500);
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
                                        List<String> list = DataUtil.createList(mCount, 15);
                                        mCount += list.size();
                                        mAdapter.appendData(list);
                                        mRefreshLayout.refreshComplete(500);
                                    }
                                },
                                2000);
                    }
                });
        mRefreshLayout.autoRefresh();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case Menu.FIRST:
                LinearLayout linearLayout =
                        findViewById(R.id.linearLayout_test_recyclerView_in_nestedScrollView);
                BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, getString(R.string.expand_or_collapse));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        startActivity(
                new Intent(
                        TestRecyclerViewInNestedScrollViewInSrlActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
