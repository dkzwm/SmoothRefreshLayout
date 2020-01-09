package me.dkzwm.widget.srl.sample.ui;

import static me.dkzwm.widget.srl.config.Constants.ACTION_NOTIFY;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.sample.BuildConfig;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.widget.WaveSmoothRefreshLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Handler mHandler = new Handler();
    private WaveSmoothRefreshLayout mRefreshLayout;
    private Button mButtonDebug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_main);
        // 设置刷新回调
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mRefreshLayout.refreshComplete(800);
                                    }
                                },
                                4000);
                    }
                });
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setDisablePerformLoadMore(true);
        mRefreshLayout.setEnableOldTouchHandling(false);
        mRefreshLayout.getFooterView().getView().setVisibility(View.GONE);
        mRefreshLayout
                .getDefaultHeader()
                .setWaveColor(ContextCompat.getColor(this, R.color.colorPrimary));
        mRefreshLayout
                .getDefaultHeader()
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
        mRefreshLayout.getDefaultHeader().setStyle(IRefreshView.STYLE_PIN);
        // 自动刷新
        mRefreshLayout.setSpringInterpolator(new OvershootInterpolator(3f));
        mHandler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.autoRefresh(ACTION_NOTIFY, true);
                    }
                },
                100);
        findViewById(R.id.imageView_main_bottom_icon).setOnClickListener(this);
        findViewById(R.id.button_main_with_frameLayout).setOnClickListener(this);
        findViewById(R.id.button_main_with_listView).setOnClickListener(this);
        findViewById(R.id.button_main_with_gridView).setOnClickListener(this);
        findViewById(R.id.button_main_with_recyclerView).setOnClickListener(this);
        findViewById(R.id.button_main_with_recyclerView_in_coordinatorLayout)
                .setOnClickListener(this);
        findViewById(R.id.button_main_test_material_style).setOnClickListener(this);
        findViewById(R.id.button_main_test_nested).setOnClickListener(this);
        findViewById(R.id.button_main_test_recyclerView_in_nestedScrollView_in_srl)
                .setOnClickListener(this);
        findViewById(R.id.button_main_test_release_to_refresh).setOnClickListener(this);
        findViewById(R.id.button_main_with_viewPager).setOnClickListener(this);
        findViewById(R.id.button_main_with_webView).setOnClickListener(this);
        findViewById(R.id.button_main_test_recyclerView_in_nestedScrollView)
                .setOnClickListener(this);
        findViewById(R.id.button_main_test_over_scroll).setOnClickListener(this);
        findViewById(R.id.button_main_test_two_level_refresh).setOnClickListener(this);
        findViewById(R.id.button_main_test_QQ_activity_style).setOnClickListener(this);
        findViewById(R.id.button_main_test_QQ_web_style).setOnClickListener(this);
        findViewById(R.id.button_main_test_nested_viewPager).setOnClickListener(this);
        findViewById(R.id.button_main_test_base_recyclerView_adapter).setOnClickListener(this);
        findViewById(R.id.button_main_test_nested_horizontal_views).setOnClickListener(this);
        findViewById(R.id.button_main_test_horizontal_refresh).setOnClickListener(this);
        findViewById(R.id.button_main_test_horizontal_recyclerView).setOnClickListener(this);
        findViewById(R.id.button_main_test_multi_direction_views).setOnClickListener(this);
        findViewById(R.id.button_main_test_scroll_to_auto_refresh).setOnClickListener(this);
        findViewById(R.id.button_main_test_scale_effect).setOnClickListener(this);
        findViewById(R.id.button_main_test_horizontal_scale_effect).setOnClickListener(this);
        findViewById(R.id.button_main_test_bottom_sheet_dialog).setOnClickListener(this);
        findViewById(R.id.button_main_test_motionLayout_scene).setOnClickListener(this);
        findViewById(R.id.button_main_test_inner_motionLayout_scene).setOnClickListener(this);
        findViewById(R.id.button_main_test_nested_with_viewPager_in_one_srl)
                .setOnClickListener(this);
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
            case R.id.button_main_test_recyclerView_in_nestedScrollView:
                startActivity(
                        new Intent(
                                MainActivity.this,
                                TestRecyclerViewInNestedScrollViewActivity.class));
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
                startActivity(
                        new Intent(
                                MainActivity.this,
                                WithRecyclerViewInCoordinatorLayoutActivity.class));
                break;
            case R.id.button_main_test_over_scroll:
                startActivity(new Intent(MainActivity.this, TestOverScrollActivity.class));
                break;
            case R.id.button_main_with_viewPager:
                startActivity(new Intent(MainActivity.this, WithViewPagerActivity.class));
                break;
            case R.id.button_main_test_recyclerView_in_nestedScrollView_in_srl:
                startActivity(
                        new Intent(
                                MainActivity.this,
                                TestRecyclerViewInNestedScrollViewInSrlActivity.class));
                break;
            case R.id.button_main_test_release_to_refresh:
                startActivity(new Intent(MainActivity.this, TestReleaseToRefreshActivity.class));
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
            case R.id.button_main_test_nested_with_viewPager_in_one_srl:
                startActivity(new Intent(MainActivity.this, TestNestedWithViewPagerActivity.class));
                break;
            case R.id.button_main_test_QQ_web_style:
                startActivity(new Intent(MainActivity.this, TestQQWebStyleActivity.class));
                break;
            case R.id.button_main_test_nested_viewPager:
                startActivity(new Intent(MainActivity.this, TestNestedViewPagerActivity.class));
                break;
            case R.id.button_main_test_base_recyclerView_adapter:
                startActivity(
                        new Intent(MainActivity.this, TestBaseRecyclerViewAdapterActivity.class));
                break;
            case R.id.button_main_test_nested_horizontal_views:
                startActivity(
                        new Intent(MainActivity.this, TestNestedHorizontalViewsActivity.class));
                break;
            case R.id.button_main_test_horizontal_recyclerView:
                startActivity(
                        new Intent(MainActivity.this, TestHorizontalRecyclerViewActivity.class));
                break;
            case R.id.button_main_test_multi_direction_views:
                startActivity(new Intent(MainActivity.this, TestMultiDirectionViewsActivity.class));
                break;
            case R.id.button_main_test_scroll_to_auto_refresh:
                startActivity(new Intent(MainActivity.this, TestScrollToAutoRefreshActivity.class));
                break;
            case R.id.imageView_main_bottom_icon:
                Toast.makeText(
                                this,
                                getString(R.string.current_version) + BuildConfig.VERSION_NAME,
                                Toast.LENGTH_SHORT)
                        .show();
                break;
            case R.id.button_main_test_horizontal_refresh:
                startActivity(new Intent(MainActivity.this, TestHorizontalRefreshActivity.class));
                break;
            case R.id.button_main_test_scale_effect:
                startActivity(new Intent(MainActivity.this, TestScaleEffectActivity.class));
                break;
            case R.id.button_main_test_horizontal_scale_effect:
                startActivity(
                        new Intent(MainActivity.this, TestHorizontalScaleEffectActivity.class));
                break;
            case R.id.button_main_test_bottom_sheet_dialog:
                startActivity(new Intent(MainActivity.this, TestBottomSheetDialogActivity.class));
                break;
            case R.id.button_main_test_motionLayout_scene:
                startActivity(new Intent(MainActivity.this, TestMotionLayoutSceneActivity.class));
                break;
            case R.id.button_main_test_inner_motionLayout_scene:
                startActivity(new Intent(MainActivity.this, TestInMotionLayoutSceneActivity.class));
                break;
            case R.id.button_main_debug:
                SmoothRefreshLayout.sDebug = !SmoothRefreshLayout.sDebug;
                if (SmoothRefreshLayout.sDebug) {
                    mButtonDebug.setText(R.string.debug_off);
                } else {
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
            else mRefreshLayout.getDefaultHeader().setStyle(IRefreshView.STYLE_SCALE);
            return true;
        }
        return false;
    }
}
