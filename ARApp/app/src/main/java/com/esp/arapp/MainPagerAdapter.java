package com.esp.arapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.esp.arapp.fragments.InstantFragment;
import com.esp.arapp.fragments.QRFragment;
import com.esp.arapp.fragments.VuforiaARFragment;
import com.esp.arapp.fragments.VuforiaRajawaliFragment;

import java.util.ArrayList;
import java.util.List;

public class MainPagerAdapter extends FragmentPagerAdapter{

    private List<Fragment> fragmentList;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
        fragmentList = new ArrayList<>();
        fragmentList.add(new QRFragment());
        fragmentList.add(new InstantFragment());
        fragmentList.add(new VuforiaARFragment());
        fragmentList.add(new VuforiaRajawaliFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
