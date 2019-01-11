package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.dkzwm.widget.srl.MaterialSmoothRefreshLayout;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/1.
 *
 * @author dkzwm
 */
public class TestMaterialStyleActivity extends AppCompatActivity {
    private MaterialSmoothRefreshLayout mRefreshLayout;
    private TextView mTextView;
    private Handler mHandler = new Handler();
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_refresh);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_material_style);
        mTextView = findViewById(R.id.textView_test_refresh_desc);
        mRefreshLayout = findViewById(R.id.smoothRefreshLayout_test_refresh);
        mRefreshLayout.materialStyle();
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
                });
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
        startActivity(new Intent(TestMaterialStyleActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
