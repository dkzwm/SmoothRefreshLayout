package me.dkzwm.widget.srl.sample.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.dkzwm.widget.srl.sample.R;

/**
 * Created by dkzwm on 2017/6/2.
 *
 * @author dkzwm
 */
public class PageFragment extends Fragment {
    private int mColor;
    private int mPage;

    public static PageFragment newInstance(int page, int color) {
        PageFragment fragment = new PageFragment();
        fragment.mColor = color;
        fragment.mPage = page;
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
