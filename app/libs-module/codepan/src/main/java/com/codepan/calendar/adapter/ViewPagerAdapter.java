package com.codepan.calendar.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {

	private List<Fragment> fragments;
	private String tabItems[];

	public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	public ViewPagerAdapter(FragmentManager fm, List<Fragment> fragments, String tabItems[]) {
		super(fm);
		this.fragments = fragments;
		this.tabItems = tabItems;
	}

	@Override
	public Fragment getItem(int position) {
		return this.fragments.get(position);
	}

	@Override
	public int getCount() {
		return this.fragments.size();
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return tabItems[position];
	}
}
