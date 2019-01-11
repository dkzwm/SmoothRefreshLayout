package me.dkzwm.widget.srl.sample.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import java.util.List;

/**
 * Created by dkzwm on 2017/6/2.
 *
 * @author dkzwm
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private List<? extends Fragment> mViewPagerFragments;

    public ViewPagerAdapter(FragmentManager fm, List<? extends Fragment> list) {
        super(fm);
        mViewPagerFragments = list;
    }

    @Override
    public Fragment getItem(int position) {
        return mViewPagerFragments.get(position);
    }

    @Override
    public int getCount() {
        return mViewPagerFragments.size();
    }
}
