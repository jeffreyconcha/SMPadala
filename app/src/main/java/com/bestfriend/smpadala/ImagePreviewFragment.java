package com.bestfriend.smpadala;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.bestfriend.constant.App;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.TouchImageView;

public class ImagePreviewFragment extends Fragment implements OnClickListener {

	private CodePanButton btnBackImagePreview;
	private TouchImageView ivImagePreview;
	private FragmentManager manager;
	private MainActivity main;
	private SQLiteAdapter db;
	private String photo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = (MainActivity) getActivity();
		manager = main.getSupportFragmentManager();
		db = main.getDatabase();
		db.openConnection();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.image_preview_layout, container, false);
		ivImagePreview = view.findViewById(R.id.ivImagePreview);
		btnBackImagePreview = view.findViewById(R.id.btnBackImagePreview);
		btnBackImagePreview.setOnClickListener(this);
		if(photo != null) {
			String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE).getPath() + "/" + photo;
			CodePanUtils.displayImage(ivImagePreview, uri);
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnBackImagePreview:
				manager.popBackStack();
				break;
		}
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}
}
