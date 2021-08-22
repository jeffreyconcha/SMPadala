package com.bestfriend.smpadala;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codepan.app.CPFragment;
import com.codepan.callback.Interface.OnCancelCallback;
import com.codepan.widget.calendar.callback.Interface.OnPickDateCallback;
import com.codepan.widget.calendar.view.CalendarView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CalendarDialogFragment extends CPFragment implements OnPickDateCallback, OnCancelCallback {

	private OnPickDateCallback pickDateCallback;
	private CalendarView cvCalendar;
	private String date;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.disableBackPressed();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.calendar_dialog_layout, container, false);
		cvCalendar = view.findViewById(R.id.cvCalendar);
		cvCalendar.setOnPickDateCallback(this);
		cvCalendar.setOnCancelCallback(this);
		cvCalendar.setCurrentDate(date);
		view.setOnClickListener(v -> {
			manager.popBackStack();
		});
		return view;
	}

	@Override
	public void onPickDate(String date) {
		manager.popBackStack();
		if (pickDateCallback != null) {
			pickDateCallback.onPickDate(date);
		}
	}

	public void setOnPickDateCallback(OnPickDateCallback pickDateCallback) {
		this.pickDateCallback = pickDateCallback;
	}

	public void setCurrentDate(String date) {
		this.date = date;
	}

	@Override
	public void onCancel() {
		manager.popBackStack();
	}
}
