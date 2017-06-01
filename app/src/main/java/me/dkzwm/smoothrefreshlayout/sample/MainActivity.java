package me.dkzwm.smoothrefreshlayout.sample;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import me.dkzwm.smoothrefreshlayout.SmoothRefreshLayout;

public class MainActivity extends AppCompatActivity {
    private Handler mHandler = new Handler();
    private SmoothRefreshLayout mRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRefreshLayout = (SmoothRefreshLayout) findViewById(R.id.smoothRefreshLayout_main);
        mRefreshLayout.setEnableOverScroll(true);
        mRefreshLayout.setMode(SmoothRefreshLayout.MODE_REFRESH);
        mRefreshLayout.setEnablePinContentView(true);
        mRefreshLayout.setEnableKeepExtraView(true);
        //设置刷新时黏贴属性
        mRefreshLayout.setEnablePinExtraViewWhileLoading(true);
        mRefreshLayout.setOnRefreshListener(new SmoothRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefreshBegin(boolean isRefresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRefreshLayout.refreshComplete();
                    }
                }, 4000);
            }

            @Override
            public void onRefreshComplete() {

            }
        });
        mRefreshLayout.autoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
