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
        //设置模式
        mRefreshLayout.setMode(SmoothRefreshLayout.MODE_BOTH);
        //开启越界回弹效果
        mRefreshLayout.setEnableOverScroll(true);
        //开启黏贴固定被刷新视图
        mRefreshLayout.setEnablePinContentView(true);
        //刷新时保持刷新视图停在其视图高度等待刷新完成
        mRefreshLayout.setEnableKeepRefreshView(true);
        //设置刷新时黏贴属性
        mRefreshLayout.setEnablePinRefreshViewWhileLoading(true);
        //设置刷新回调
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
        //自动刷新
        mRefreshLayout.autoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
