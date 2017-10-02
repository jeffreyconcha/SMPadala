package com.codepan.widget;

import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.codepan.model.EntityObj;

import java.util.ArrayList;

public class AdapterFilter<T extends EntityObj> extends Filter {

	private ArrayAdapter<T> adapter;
	private ArrayList<T> allItems;
	private ArrayList<T> items;

	public AdapterFilter(ArrayList<T> items, ArrayAdapter<T> adapter) {
		this.allItems = new ArrayList<>();
		this.allItems.addAll(items);
		this.adapter = adapter;
		this.items = items;
	}

	@Override
	public CharSequence convertResultToString(Object result) {
		return ((EntityObj) result).name;
	}

	@Override
	protected FilterResults performFiltering(CharSequence cs) {
		FilterResults results = null;
		if(cs != null) {
			ArrayList<T> suggestList = new ArrayList<>();
			for(T entity : allItems) {
				String name = entity.name;
				if(name != null) {
					String text = name.toLowerCase();
					String search = cs.toString().toLowerCase();
					if(text.contains(search)) {
						suggestList.add(entity);
					}
				}
			}
			if(!suggestList.isEmpty()) {
				results = new FilterResults();
				results.values = suggestList;
				results.count = suggestList.size();
			}
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void publishResults(CharSequence cs, FilterResults fr) {
		items.clear();
		if(fr != null && fr.count > 0) {
			items.addAll((ArrayList<T>) fr.values);
		}
		adapter.notifyDataSetChanged();
	}
}
