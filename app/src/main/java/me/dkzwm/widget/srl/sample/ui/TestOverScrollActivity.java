package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class TestOverScrollActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_overscroll);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.over_scroll);
        final SmoothRefreshLayout refreshLayout =
                findViewById(R.id.smoothRefreshLayout_test_over_scroll1);
        refreshLayout.setDisableLoadMore(false);
        refreshLayout.setDisablePerformRefresh(true);
        refreshLayout.setDisablePerformLoadMore(true);
        refreshLayout.setEnableKeepRefreshView(false);
        refreshLayout.getHeaderView().getView().setVisibility(View.GONE);
        refreshLayout.getFooterView().getView().setVisibility(View.GONE);
        final SmoothRefreshLayout refreshLayout2 =
                findViewById(R.id.smoothRefreshLayout_test_over_scroll2);
        refreshLayout2.setDisableLoadMore(false);
        refreshLayout2.setDisablePerformRefresh(true);
        refreshLayout2.setDisablePerformLoadMore(true);
        refreshLayout2.setEnableKeepRefreshView(false);
        refreshLayout2.getHeaderView().getView().setVisibility(View.GONE);
        refreshLayout2.getFooterView().getView().setVisibility(View.GONE);
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
        startActivity(new Intent(TestOverScrollActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
