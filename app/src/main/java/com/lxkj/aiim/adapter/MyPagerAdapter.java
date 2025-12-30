package com.lxkj.aiim.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kxn on 2021/11/30 0030.
 */
public class MyPagerAdapter extends FragmentStateAdapter {

    private List<Fragment> mFragmentList = new ArrayList<>();

    public void setFragments(ArrayList<Fragment> fragments) {
        mFragmentList.clear();
        mFragmentList.addAll(fragments);
        notifyDataSetChanged();
    }

    public MyPagerAdapter(Fragment fm) {
        super(fm);
    }





    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment = mFragmentList.get(position);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return mFragmentList.size();
    }
    // 强烈建议加上这个，防止 RecyclerView 复用时出现 Fragment 错乱
    @Override
    public long getItemId(int position) {
        return mFragmentList.get(position).hashCode(); // 或者用 position
    }

    @Override
    public boolean containsItem(long itemId) {
        for (Fragment fragment : mFragmentList) {
            if (fragment.hashCode() == itemId) {
                return true;
            }
        }
        return false;
    }
}
