package me.dkzwm.widget.srl.sample.ui;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

public class TestInMotionLayoutSceneActivity extends AppCompatActivity {
    private Handler mHandler = new Handler();
    private SmoothRefreshLayout mRefreshLayout;
    private MotionLayout mMotionLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_inner_motionscene_start);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_inner_motionlayout_scene);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_test_motionLayout_scene);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this, getLayoutInflater());
        recyclerView.setAdapter(adapter);
        adapter.updateData(DataUtil.createList(0, 100));
        mMotionLayout = findViewById(R.id.motionLayout_test_motionLayout_scene);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_motionLayout_scene);
        mRefreshLayout.setOnHeaderEdgeDetectCallBack(
                new SmoothRefreshLayout.OnHeaderEdgeDetectCallBack() {
                    @Override
                    public boolean isNotYetInEdgeCannotMoveHeader(
                            SmoothRefreshLayout parent,
                            @Nullable View child,
                            @Nullable IRefreshView header) {
                        return mMotionLayout.getProgress() != 0f
                                || (child != null && child.canScrollVertically(-1));
                    }
                });
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                1000);
                    }
                });
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
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
