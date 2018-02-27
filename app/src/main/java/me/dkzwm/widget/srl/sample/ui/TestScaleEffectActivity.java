package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.ListView;

import java.util.List;

import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.config.Constants;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.ListViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/**
 * Created by dkzwm on 2018/2/27.
 *
 * @author dkzwm
 */
public class TestScaleEffectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scale_effect);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.with_listView);
        ListView listView = findViewById(R.id.listView_test_scale_effect);
        ListViewAdapter adapter = new ListViewAdapter(this, getLayoutInflater());
        List<String> list = DataUtil.createList(0, 40);
        adapter.updateData(list);
        listView.setAdapter(adapter);
        SmoothRefreshLayout refreshLayout = findViewById(R.id.smoothRefreshLayout_test_scale_effect);
        refreshLayout.setDisableLoadMore(false);
        refreshLayout.setMode(Constants.MODE_SCALE);
        refreshLayout.setDurationToClose(800);
        Interpolator interpolator=new BounceInterpolator();
        refreshLayout.setSpringInterpolator(interpolator);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(TestScaleEffectActivity.this, MainActivity.class));
        finish();
    }

}

