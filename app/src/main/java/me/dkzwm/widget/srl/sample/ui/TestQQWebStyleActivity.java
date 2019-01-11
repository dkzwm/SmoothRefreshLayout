package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.header.CustomQQWebHeader;

/**
 * Created by dkzwm on 2017/6/27.
 *
 * @author dkzwm
 */
public class TestQQWebStyleActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private WebView mWebView;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_webview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_qq_web_style);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_webView);
        mRefreshLayout.setHeaderView(new CustomQQWebHeader(this));
        mRefreshLayout.setDisablePerformRefresh(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setDisablePerformLoadMore(true);
        mRefreshLayout.getFooterView().getView().setVisibility(View.GONE);
        mRefreshLayout.setEnableHeaderDrawerStyle(true);
        mRefreshLayout.setMaxMoveRatioOfHeader(1);
        mRefreshLayout.setEnableKeepRefreshView(false);
        mWebView = findViewById(R.id.webView_with_webView);
        mWebView.loadUrl("https://github.com/dkzwm");
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
        startActivity(new Intent(TestQQWebStyleActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWebView != null) {
            mWebView.removeAllViews();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.setTag(null);
            mWebView.clearHistory();
            mWebView.destroy();
            mWebView = null;
        }
        mHandler.removeCallbacksAndMessages(null);
    }
}
