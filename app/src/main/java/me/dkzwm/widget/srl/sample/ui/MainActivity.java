package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.indicator.IIndicator;
import me.dkzwm.widget.srl.sample.BuildConfig;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.widget.WaveSmoothRefreshLayout;
import me.dkzwm.widget.srl.utils.SRLog;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler mHandler = new Handler();
    private WaveSmoothRefreshLayout mRefreshLayout;
    private Button mButtonDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_main);
        //设置刷新回调
        mRefreshLayout.setOnRefreshListener(new RefreshingListenerAdapter() {
            @Override
            public void onRefreshBegin(boolean isRefresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete(800);
                    }
                }, 4000);
            }
        });
        mRefreshLayout.addOnUIPositionChangedListener(new SmoothRefreshLayout
                .OnUIPositionChangedListener() {
            @Override
            public void onChanged(byte status, IIndicator indicator) {
                if (!mRefreshLayout.isOverScrolling()
                        && indicator.getMovingStatus() == IIndicator.MOVING_FOOTER) {
                    mRefreshLayout.resetScrollerInterpolator();
                }
            }
        });
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setDisablePerformLoadMore(true);
        mRefreshLayout.setEnableHideFooterView(true);
        mRefreshLayout.getDefaultHeader().setWaveColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mRefreshLayout.getDefaultHeader().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        mRefreshLayout.getDefaultHeader().setStyle(IRefreshView.STYLE_PIN);
        //自动刷新
        mRefreshLayout.autoRefresh(true, false);
        findViewById(R.id.imageView_main_bottom_icon).setOnClickListener(this);
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
        findViewById(R.id.button_main_test_QQ_web_style).setOnClickListener(this);
        findViewById(R.id.button_main_test_nested_view_pager).setOnClickListener(this);
        findViewById(R.id.button_main_test_base_recyclerView_adapter).setOnClickListener(this);
        findViewById(R.id.button_main_test_nested_horizontal_views).setOnClickListener(this);
        findViewById(R.id.button_main_test_horizontal_refresh).setOnClickListener(this);
        findViewById(R.id.button_main_test_horizontal_recyclerView).setOnClickListener(this);
        findViewById(R.id.button_main_test_multi_direction_views).setOnClickListener(this);
        findViewById(R.id.button_main_test_scroll_to_auto_refresh).setOnClickListener(this);
        mButtonDebug = findViewById(R.id.button_main_debug);
        mButtonDebug.setOnClickListener(this);
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
                startActivity(new Intent(MainActivity.this, TestQQActivityStyleActivity.class));
                break;
            case R.id.button_main_test_nested:
                startActivity(new Intent(MainActivity.this, TestNestedActivity.class));
                break;
            case R.id.button_main_test_QQ_web_style:
                startActivity(new Intent(MainActivity.this, TestQQWebStyleActivity.class));
                break;
            case R.id.button_main_test_nested_view_pager:
                startActivity(new Intent(MainActivity.this, TestNestedViewPagerActivity.class));
                break;
            case R.id.button_main_test_base_recyclerView_adapter:
                startActivity(new Intent(MainActivity.this, TestBaseRecyclerViewAdapterActivity.class));
                break;
            case R.id.button_main_test_nested_horizontal_views:
                startActivity(new Intent(MainActivity.this, TestNestedHorizontalViewsActivity.class));
                break;
            case R.id.button_main_test_horizontal_recyclerView:
                startActivity(new Intent(MainActivity.this, TestHorizontalRecyclerViewActivity.class));
                break;
            case R.id.button_main_test_multi_direction_views:
                startActivity(new Intent(MainActivity.this, TestMultiDirectionViewsActivity.class));
                break;
            case R.id.button_main_test_scroll_to_auto_refresh:
                startActivity(new Intent(MainActivity.this, TestScrollToAutoRefreshActivity.class));
                break;
            case R.id.imageView_main_bottom_icon:
                Toast.makeText(this, getString(R.string.current_version) + BuildConfig.VERSION_NAME,
                        Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_main_test_horizontal_refresh:
                startActivity(new Intent(MainActivity.this, TestHorizontalRefreshActivity.class));
                break;
            case R.id.button_main_debug:
                SmoothRefreshLayout.debug(!SmoothRefreshLayout.isDebug());
                if (SmoothRefreshLayout.isDebug()) {
                    SRLog.setLevel(SRLog.LEVEL_VERBOSE);
                    mButtonDebug.setText(R.string.debug_off);
                } else {
                    SRLog.setLevel(SRLog.LEVEL_WARNING);
                    mButtonDebug.setText(R.string.debug_on);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.change_style);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == Menu.FIRST) {
            if (mRefreshLayout.getDefaultHeader().getStyle() == IRefreshView.STYLE_SCALE)
                mRefreshLayout.getDefaultHeader().setStyle(IRefreshView.STYLE_PIN);
            else
                mRefreshLayout.getDefaultHeader().setStyle(IRefreshView.STYLE_SCALE);
            return true;
        }
        return false;
    }
}
