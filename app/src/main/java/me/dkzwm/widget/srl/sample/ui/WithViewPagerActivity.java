package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import com.ToxicBakery.viewpager.transforms.DrawerTransformer;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.header.MaterialHeader;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.ViewPagerAdapter;
import me.dkzwm.widget.srl.sample.ui.fragment.PageFragment;
import me.dkzwm.widget.srl.util.PixelUtl;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithViewPagerActivity extends AppCompatActivity {
    private static final int[] sColors =
            new int[] {Color.WHITE, Color.GREEN, Color.YELLOW, Color.BLUE, Color.RED, Color.BLACK};
    private SmoothRefreshLayout mRefreshLayout;
    private ViewPager mViewPager;
    private ViewPagerAdapter mAdapter;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_viewpager);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_viewPager);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_viewPager);
        MaterialHeader header = new MaterialHeader(this);
        header.setPadding(0, PixelUtl.dp2px(this, 20), 0, PixelUtl.dp2px(this, 20));
        mRefreshLayout.setHeaderView(header);
        mRefreshLayout.setEnablePinContentView(true);
        mRefreshLayout.setEnableKeepRefreshView(true);
        mRefreshLayout.setDisableWhenAnotherDirectionMove(true);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        mViewPager.setCurrentItem(0, true);
                                        mRefreshLayout.refreshComplete();
                                        Toast.makeText(
                                                        WithViewPagerActivity.this,
                                                        R.string.sr_refresh_complete,
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                },
                                4000);
                    }
                });
        mViewPager = findViewById(R.id.viewPager_with_viewPager);
        List<PageFragment> fragments = new ArrayList<>();
        for (int i = 0; i < sColors.length; i++) {
            fragments.add(PageFragment.newInstance(i, sColors[i]));
        }
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageTransformer(true, new DrawerTransformer());
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
        startActivity(new Intent(WithViewPagerActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
