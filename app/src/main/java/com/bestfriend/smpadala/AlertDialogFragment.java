package com.bestfriend.smpadala;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.codepan.app.CPFragment;
import com.codepan.utils.CodePanUtils;
import com.codepan.utils.SpannableMap;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;

import java.util.ArrayList;

public class AlertDialogFragment extends CPFragment {

	private String dialogTitle, dialogMessage, positiveButtonTitle, negativeButtonTitle;
	private OnClickListener positiveButtonOnClick, negativeButtonOnClick;
	public CodePanButton btnPositiveAlertDialog, btnNegativeAlertDialog;
	private CodePanLabel tvMessageAlertDialog, tvTitleAlertDialog;
	private int positiveButtonVisibility = View.GONE;
	private int negativeButtonVisibility = View.GONE;
	private int dialogMessageVisibility = View.VISIBLE;
	private int dialogTitleVisibility = View.GONE;
	private ArrayList<SpannableMap> list;
	private int title, message;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.disableBackPressed();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.alert_dialog_layout, container, false);
		btnPositiveAlertDialog = view.findViewById(R.id.btnPositiveAlertDialog);
		btnNegativeAlertDialog = view.findViewById(R.id.btnNegativeAlertDialog);
		tvTitleAlertDialog = view.findViewById(R.id.tvTitleAlertDialog);
		tvMessageAlertDialog = view.findViewById(R.id.tvMessageAlertDialog);
		dialogTitle = dialogTitle != null ? dialogTitle : getString(title);
		dialogMessage = dialogMessage != null ? dialogMessage : getString(message);
		if (list != null) {
			SpannableStringBuilder ssb = CodePanUtils.customizeText(list, dialogMessage);
			tvMessageAlertDialog.setText(ssb);
		}
		else {
			tvMessageAlertDialog.setText(dialogMessage);
		}
		tvTitleAlertDialog.setText(dialogTitle);
		btnPositiveAlertDialog.setText(positiveButtonTitle);
		btnNegativeAlertDialog.setText(negativeButtonTitle);
		btnPositiveAlertDialog.setOnClickListener(positiveButtonOnClick);
		btnNegativeAlertDialog.setOnClickListener(negativeButtonOnClick);
		btnPositiveAlertDialog.setVisibility(positiveButtonVisibility);
		btnNegativeAlertDialog.setVisibility(negativeButtonVisibility);
		tvMessageAlertDialog.setVisibility(dialogMessageVisibility);
		tvTitleAlertDialog.setVisibility(dialogTitleVisibility);
		return view;
	}

	public void setDialogMessage(String dialogMessage) {
		this.dialogMessage = dialogMessage;
	}

	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
		this.dialogTitleVisibility = View.VISIBLE;
	}

	public void setDialogTitle(int resId) {
		this.dialogTitleVisibility = View.VISIBLE;
		this.title = resId;
	}

	public void setDialogMessage(int resId) {
		this.message = resId;
	}

	public void hideDialogMessage() {
		this.dialogMessageVisibility = View.GONE;
	}

	public void setSpannableList(ArrayList<SpannableMap> list) {
		this.list = list;
	}

	public void setPositiveButton(String positiveButtonTitle, OnClickListener onClick) {
		this.positiveButtonVisibility = View.VISIBLE;
		this.positiveButtonTitle = positiveButtonTitle;
		this.positiveButtonOnClick = onClick;
	}

	public void setNegativeButton(String negativeButtonTitle, OnClickListener onClick) {
		this.negativeButtonVisibility = View.VISIBLE;
		this.negativeButtonTitle = negativeButtonTitle;
		this.negativeButtonOnClick = onClick;
	}
}
