package me.dkzwm.widget.srl.sample.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/2.
 *
 * @author dkzwm
 */
public class PageFragment extends Fragment {
    private int mPage;
    private int mColor;

    public static PageFragment newInstance(int page, int color) {
        PageFragment fragment = new PageFragment();
        fragment.mPage = page;
        fragment.mColor = color;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page, container, false);
        view.findViewById(R.id.frameLayout_page).setBackgroundColor(mColor);
        TextView textView = view.findViewById(R.id.textView_page);
        textView.setText(String.valueOf(mPage));
        return view;
    }
}
