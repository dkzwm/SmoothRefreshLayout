package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import java.util.ArrayList;
import java.util.List;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.ViewPagerAdapter;
import me.dkzwm.widget.srl.sample.ui.fragment.NestedPageWithSrlFragment;

/** Created by dkzwm on 2017/7/4. @@author dkzwm */
public class TestNestedViewPagerActivity extends AppCompatActivity {
    private static final int[] sColors =
            new int[] {Color.WHITE, Color.GREEN, Color.YELLOW, Color.BLUE, Color.RED, Color.BLACK};
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_with_nested_viewpager);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.arrow_back_white_72x72);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
        ViewPager viewPager = findViewById(R.id.viewPager_with_nested_pager);
        List<NestedPageWithSrlFragment> fragments = new ArrayList<>();
        for (int color : sColors) {
            fragments.add(NestedPageWithSrlFragment.newInstance(color));
        }
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
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
        startActivity(new Intent(TestNestedViewPagerActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
