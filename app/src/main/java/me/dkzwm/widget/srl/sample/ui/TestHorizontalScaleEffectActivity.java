package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.animation.Interpolator;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.manager.HScaleLayoutManager;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2018/2/27.
 *
 * @author dkzwm
 */
public class TestHorizontalScaleEffectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_horizontal_scale_effect);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_horizontal_scale_effect);
        SmoothRefreshLayout refreshLayout =
                findViewById(R.id.smoothRefreshLayout_test_horizontal_scale_effect);
        refreshLayout.setDisableLoadMore(false);
        refreshLayout.setDurationToClose(800);
        refreshLayout.setLayoutManager(new HScaleLayoutManager());
        Interpolator interpolator =
                new Interpolator() {
                    @Override
                    public float getInterpolation(float input) {
                        return (float) (--input * input * ((1.7 + 1f) * input + 1.7) + 1f);
                    }
                };
        refreshLayout.setSpringBackInterpolator(interpolator);
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
        startActivity(new Intent(TestHorizontalScaleEffectActivity.this, MainActivity.class));
        finish();
    }
}
