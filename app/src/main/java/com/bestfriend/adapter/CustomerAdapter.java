package com.bestfriend.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;

import com.bestfriend.constant.App;
import com.bestfriend.model.CustomerObj;
import com.bestfriend.smpadala.R;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.AdapterFilter;
import com.codepan.widget.CodePanLabel;

import java.util.ArrayList;

public class CustomerAdapter extends ArrayAdapter<CustomerObj> {

	private AdapterFilter<CustomerObj> filter;
	private ArrayList<CustomerObj> items;
	private LayoutInflater inflater;
	private String path;

	public CustomerAdapter(Context context, ArrayList<CustomerObj> items) {
		super(context, 0, items);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.items = items;
		this.path = "file://" + context.getDir(App.FOLDER, Context.MODE_PRIVATE).getPath() + "/";
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		final CustomerObj obj = items.get(position);
		if(obj != null) {
			if(view == null) {
				view = inflater.inflate(R.layout.customer_list_row, parent, false);
				holder = new ViewHolder();
				holder.tvNameCustomer = view.findViewById(R.id.tvNameCustomer);
				holder.ivPhotoCustomer = view.findViewById(R.id.ivPhotoCustomer);
				view.setTag(holder);
			}
			else {
				holder = (ViewHolder) view.getTag();
			}
			holder.tvNameCustomer.setText(obj.name);
			String uri = path + obj.photo;
			CodePanUtils.displayImage(holder.ivPhotoCustomer, uri, R.drawable.ic_user);
		}
		return view;
	}

	@NonNull
	@Override
	public Filter getFilter() {
		if(filter == null) {
			filter = new AdapterFilter<>(items, this);
		}
		return filter;
	}

	private class ViewHolder {
		private CodePanLabel tvNameCustomer;
		private ImageView ivPhotoCustomer;
	}
}
