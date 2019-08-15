package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.IRefreshView;
import me.dkzwm.widget.srl.extra.footer.ClassicFooter;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.header.StoreHouseHeader;
import me.dkzwm.widget.srl.util.PixelUtl;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class TestReleaseToRefreshActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private TextView mTextView;
    private Handler mHandler = new Handler();
    private int mCount = 0;
    private StoreHouseHeader mStoreHouseHeader;
    private ClassicFooter mClassicFooter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_refresh);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.release_to_refresh);
        mTextView = findViewById(R.id.textView_test_refresh_desc);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_refresh);
        mStoreHouseHeader = new StoreHouseHeader(this);
        mStoreHouseHeader.initPathWithString(
                "RELEASE TO REFRESH", PixelUtl.dp2px(this, 18), PixelUtl.dp2px(this, 24));
        mStoreHouseHeader.setTextColor(Color.WHITE);
        mStoreHouseHeader.setPadding(0, PixelUtl.dp2px(this, 20), 0, PixelUtl.dp2px(this, 20));
        mRefreshLayout.setHeaderView(mStoreHouseHeader);
        mClassicFooter = new ClassicFooter<>(mRefreshLayout.getContext());
        mClassicFooter.setLastUpdateTimeKey("footer_last_update_time");
        mRefreshLayout.setFooterView(mClassicFooter);
        mRefreshLayout.setRatioToKeepHeader(1);
        mRefreshLayout.setRatioOfHeaderToRefresh(1);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mCount++;
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mRefreshLayout.refreshComplete();
                                        String times =
                                                getString(R.string.number_of_refresh) + mCount;
                                        mTextView.setText(times);
                                    }
                                },
                                2000);
                    }

                    @Override
                    public void onLoadingMore() {
                        mCount++;
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mRefreshLayout.refreshComplete();
                                        String times =
                                                getString(R.string.number_of_refresh) + mCount;
                                        mTextView.setText(times);
                                    }
                                },
                                2000);
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
            case Menu.FIRST:
                mStoreHouseHeader.setStyle(IRefreshView.STYLE_DEFAULT);
                mClassicFooter.setStyle(IRefreshView.STYLE_DEFAULT);
                return true;
            case Menu.FIRST + 1:
                mStoreHouseHeader.setStyle(IRefreshView.STYLE_SCALE);
                mClassicFooter.setStyle(IRefreshView.STYLE_SCALE);
                return true;
            case Menu.FIRST + 2:
                mStoreHouseHeader.setStyle(IRefreshView.STYLE_PIN);
                mClassicFooter.setStyle(IRefreshView.STYLE_PIN);
                return true;
            case Menu.FIRST + 3:
                mStoreHouseHeader.setStyle(IRefreshView.STYLE_FOLLOW_SCALE);
                mClassicFooter.setStyle(IRefreshView.STYLE_FOLLOW_SCALE);
                return true;
            case Menu.FIRST + 4:
                mStoreHouseHeader.setStyle(IRefreshView.STYLE_FOLLOW_PIN);
                mClassicFooter.setStyle(IRefreshView.STYLE_FOLLOW_PIN);
                return true;
            case Menu.FIRST + 5:
                mStoreHouseHeader.setStyle(IRefreshView.STYLE_FOLLOW_CENTER);
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
        startActivity(new Intent(TestReleaseToRefreshActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
