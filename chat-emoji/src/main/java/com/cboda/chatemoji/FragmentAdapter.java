package com.cboda.chatemoji;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

@Deprecated
public class FragmentAdapter extends FragmentPagerAdapter {
    private final ArrayList<Fragment> fragmentList;

    public FragmentAdapter(ArrayList<Fragment> fragmentList , FragmentManager fragmentManager) {
        super(fragmentManager);

        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(@NonNull View container, int position, @NonNull Object object) {

    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
