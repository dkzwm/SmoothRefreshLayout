package me.dkzwm.widget.srl.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;
import me.dkzwm.widget.srl.RefreshingListenerAdapter;
import me.dkzwm.widget.srl.SmoothRefreshLayout;
import me.dkzwm.widget.srl.sample.R;
import me.dkzwm.widget.srl.sample.adapter.RecyclerViewAdapter;
import me.dkzwm.widget.srl.sample.utils.DataUtil;

/** @author dkzwm */
public class TestBottomSheetDialogActivity extends AppCompatActivity {
    private SmoothRefreshLayout mRefreshLayout;
    private RecyclerViewAdapter mAdapter;
    private Handler mHandler = new Handler();
    private BottomSheetDialog mSheetDialog;
    private View mRootView;
    private int mCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_bottom_sheet_dialog);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.test_bottom_sheet_dialog);
        findViewById(R.id.button_test_bottom_sheet_dialog_open)
                .setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (mSheetDialog == null) {
                                    mSheetDialog = new BottomSheetDialog(v.getContext());
                                    mSheetDialog.setContentView(mRootView);
                                    FrameLayout bottomSheet =
                                            mSheetDialog.findViewById(
                                                    com.google
                                                            .android
                                                            .material
                                                            .R
                                                            .id
                                                            .design_bottom_sheet);
                                    if (bottomSheet != null) {
                                        ViewGroup.LayoutParams layoutParams =
                                                bottomSheet.getLayoutParams();
                                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                        bottomSheet.setLayoutParams(layoutParams);
                                    }
                                }
                                mSheetDialog.show();
                            }
                        });
        mRootView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet, null);
        RecyclerView recyclerView =
                mRootView.findViewById(R.id.recyclerView_test_bottom_sheet_dialog);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerViewAdapter(this, getLayoutInflater());
        List<String> list = DataUtil.createList(mCount, 60);
        mCount = list.size();
        mAdapter.updateData(list);
        recyclerView.setAdapter(mAdapter);
        mRefreshLayout = mRootView.findViewById(R.id.smoothRefreshLayout_test_bottom_sheet_dialog);
        mRefreshLayout.setDisableRefresh(true);
        mRefreshLayout.setDisableLoadMore(false);
        mRefreshLayout.setEnableAutoLoadMore(true);
        mRefreshLayout.setOnRefreshListener(
                new RefreshingListenerAdapter() {
                    @Override
                    public void onLoadingMore() {
                        mHandler.postDelayed(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        List<String> list = DataUtil.createList(mCount, 15);
                                        mCount += list.size();
                                        mAdapter.appendData(list);
                                        mRefreshLayout.refreshComplete();
                                    }
                                },
                                2000);
                    }
                });
        mRefreshLayout.setDisableLoadMoreWhenContentNotFull(true);
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
        startActivity(new Intent(TestBottomSheetDialogActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
