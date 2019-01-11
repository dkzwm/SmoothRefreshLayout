package me.dkzwm.widget.srl.sample.ui;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.dkzwm.widget.srl.sample.R;

public class SecondFloorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_floor);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.second_floor);
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
}
