package me.dkzwm.smoothrefreshlayout.sample.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

import me.dkzwm.smoothrefreshlayout.sample.ui.fragment.PageFragment;

/**
 * Created by dkzwm on 2017/6/2.
 *
 * @author dkzwm
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private List<PageFragment> mViewPagerFragments;

    public ViewPagerAdapter(FragmentManager fm, List<PageFragment> list) {
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