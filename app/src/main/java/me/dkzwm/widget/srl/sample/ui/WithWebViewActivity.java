package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.extra.header.MaterialHeader;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.util.AutoRefreshUtil;
import me.dkzwm.widget.srl.util.PixelUtl;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class WithWebViewActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private WebView mWebView;
    private Handler mHandler = new Handler();
    private AutoRefreshUtil mAutoRefreshUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_webview);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_webView);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_with_webView);
        MaterialHeader header = new MaterialHeader(this);
        header.setPadding(0, PixelUtl.dp2px(this, 20), 0, PixelUtl.dp2px(this, 20));
        mRefreshLayout.setHeaderView(header);
        mRefreshLayout.setEnablePinRefreshViewWhileLoading(true);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onRefreshing() {
                        mWebView.loadUrl("https://github.com/dkzwm");
                    }
                });
        mWebView = findViewById(R.id.webView_with_webView);
        mWebView.setWebViewClient(
                new WebViewClient() {
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        mRefreshLayout.refreshComplete();
                    }
                });
        mRefreshLayout.autoRefresh(false);
        mAutoRefreshUtil = new AutoRefreshUtil(mWebView);
        mRefreshLayout.addLifecycleObserver(mAutoRefreshUtil);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case Menu.FIRST:
                mAutoRefreshUtil.autoRefresh(false, true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST, Menu.NONE, R.string.auto_refresh_func_demo);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(WithWebViewActivity.this, MainActivity.class));
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
