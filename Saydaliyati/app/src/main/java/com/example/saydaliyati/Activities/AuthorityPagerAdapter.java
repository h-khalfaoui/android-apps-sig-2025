package com.example.saydaliyati.Activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.saydaliyati.Fragments.AdminCalendarFragment;
import com.example.saydaliyati.Fragments.AdminDashboardFragment;
import com.example.saydaliyati.Fragments.AdminPharmaciesFragment;

public class AuthorityPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_PAGES = 3;

    public AuthorityPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new AdminDashboardFragment();
            case 1:
                return new AdminPharmaciesFragment();
            case 2:
                return new AdminCalendarFragment();
            default:
                return new AdminDashboardFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }
}