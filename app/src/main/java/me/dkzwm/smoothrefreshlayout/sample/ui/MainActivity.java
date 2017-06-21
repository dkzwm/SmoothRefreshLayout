package me.dkzwm.smoothrefreshlayout.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

import me.dkzwm.smoothrefreshlayout.RefreshingListenerAdapter;
import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;
import me.dkzwm.smoothrefreshlayout.sample.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler mHandler = new Handler();
    private SmoothRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRefreshLayout = (SmoothRefreshLayout) findViewById(R.id.smoothRefreshLayout_main);
        //设置模式
        mRefreshLayout.setMode(SmoothRefreshLayout.MODE_REFRESH);
        //开启越界回弹效果
        mRefreshLayout.setEnableOverScroll(true);
        //开启黏贴固定被刷新视图
        mRefreshLayout.setEnablePinContentView(true);
        //刷新时保持刷新视图停在其视图高度等待刷新完成
        mRefreshLayout.setEnableKeepRefreshView(true);
        //设置刷新时黏贴属性
        mRefreshLayout.setEnablePinRefreshViewWhileLoading(true);
        //设置刷新完成后可立即开始下次刷新
        mRefreshLayout.setEnabledNextPtrAtOnce(true);
        //设置刷新回调
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(boolean isRefresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete();
                    }
                }, 4000);
            }
        });
        //自动刷新
        mRefreshLayout.autoRefresh();

        findViewById(R.id.button_main_with_frameLayout).setOnClickListener(this);
        findViewById(R.id.button_main_with_listView).setOnClickListener(this);
        findViewById(R.id.button_main_with_gridView).setOnClickListener(this);
        findViewById(R.id.button_main_with_recyclerView).setOnClickListener(this);
        findViewById(R.id.button_main_with_recyclerView_in_coordinatorLayout).setOnClickListener
                (this);
        findViewById(R.id.button_main_test_enable_next_pull_to_refresh_at_once)
                .setOnClickListener(this);
        findViewById(R.id.button_main_test_material_style)
                .setOnClickListener(this);
        findViewById(R.id.button_main_test_nested).setOnClickListener(this);
        findViewById(R.id.button_main_test_pull_to_refresh).setOnClickListener(this);
        findViewById(R.id.button_main_test_release_to_refresh).setOnClickListener(this);
        findViewById(R.id.button_main_with_viewPager).setOnClickListener(this);
        findViewById(R.id.button_main_with_webView).setOnClickListener(this);
        findViewById(R.id.button_main_with_textView).setOnClickListener(this);
        findViewById(R.id.button_main_test_over_scroll).setOnClickListener(this);
        findViewById(R.id.button_main_test_two_level_refresh).setOnClickListener(this);
        findViewById(R.id.button_main_test_QQ_activity_style).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_main_with_frameLayout:
                startActivity(new Intent(MainActivity.this, WithFrameLayoutActivity.class));
                break;
            case R.id.button_main_with_textView:
                startActivity(new Intent(MainActivity.this, WithTextViewActivity.class));
                break;
            case R.id.button_main_with_webView:
                startActivity(new Intent(MainActivity.this, WithWebViewActivity.class));
                break;
            case R.id.button_main_with_listView:
                startActivity(new Intent(MainActivity.this, WithListViewActivity.class));
                break;
            case R.id.button_main_with_gridView:
                startActivity(new Intent(MainActivity.this, WithGridViewActivity.class));
                break;
            case R.id.button_main_with_recyclerView:
                startActivity(new Intent(MainActivity.this, WithRecyclerViewActivity.class));
                break;
            case R.id.button_main_with_recyclerView_in_coordinatorLayout:
                startActivity(new Intent(MainActivity.this, WithRecyclerViewInCoordinatorLayoutActivity.class));
                break;
            case R.id.button_main_test_over_scroll:
                startActivity(new Intent(MainActivity.this, TestOverScrollActivity.class));
                break;
            case R.id.button_main_with_viewPager:
                startActivity(new Intent(MainActivity.this, WithViewPagerActivity.class));
                break;
            case R.id.button_main_test_pull_to_refresh:
                startActivity(new Intent(MainActivity.this, TestPullToRefreshActivity.class));
                break;
            case R.id.button_main_test_release_to_refresh:
                startActivity(new Intent(MainActivity.this, TestReleaseToRefreshActivity.class));
                break;
            case R.id.button_main_test_enable_next_pull_to_refresh_at_once:
                startActivity(new Intent(MainActivity.this, TestNextRefreshAtOnceActivity.class));
                break;
            case R.id.button_main_test_material_style:
                startActivity(new Intent(MainActivity.this, TestMaterialStyleActivity.class));
                break;
            case R.id.button_main_test_two_level_refresh:
                startActivity(new Intent(MainActivity.this, TestTwoLevelRefreshActivity.class));
                break;
            case R.id.button_main_test_QQ_activity_style:
                startActivity(new Intent(MainActivity.this,TestQQActivityStyleActivity.class));
                break;
        }

    }
}
