package com.bestfriend.smpadala;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.bestfriend.adapter.CustomerAdapter;
import com.bestfriend.callback.Interface.OnReceiveRemittanceCallback;
import com.bestfriend.callback.Interface.OnUsePhotoCallback;
import com.bestfriend.constant.App;
import com.bestfriend.constant.Result;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.ReceiveObj;
import com.bestfriend.model.RemittanceObj;
import com.codepan.cache.TypefaceCache;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CodePanTextField;

import java.text.NumberFormat;
import java.util.ArrayList;

public class ReceiveFragment extends Fragment implements OnClickListener, TextWatcher {

	private CodePanLabel tvDateReceive, tvTimeReceive, tvAmountReceive, tvRefNoReceive,
			tvNameTitleReceive;
	private CodePanButton btnCancelReceive, btnClaimReceive, btnPhotoReceive;
	private OnReceiveRemittanceCallback receiveRemittanceCallback;
	private CodePanTextField etMobileNoReceive, etAddressReceive;
	private ArrayList<CustomerObj> customerList;
	private AutoCompleteTextView etNameReceive;
	private FragmentTransaction transaction;
	private CustomerAdapter adapter;
	private ImageView ivPhotoReceive;
	private RemittanceObj remittance;
	private FragmentManager manager;
	private CustomerObj customer;
	private boolean withChanges;
	private MainActivity main;
	private SQLiteAdapter db;
	private NumberFormat nf;
	private String photo;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		main = (MainActivity) getActivity();
		manager = main.getSupportFragmentManager();
		db = main.getDatabase();
		db.openConnection();
		nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setMinimumFractionDigits(2);
		nf.setGroupingUsed(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.receive_layout, container, false);
		tvNameTitleReceive = (CodePanLabel) view.findViewById(R.id.tvNameTitleReceive);
		tvDateReceive = (CodePanLabel) view.findViewById(R.id.tvDateReceive);
		tvTimeReceive = (CodePanLabel) view.findViewById(R.id.tvTimeReceive);
		tvAmountReceive = (CodePanLabel) view.findViewById(R.id.tvAmountReceive);
		tvRefNoReceive = (CodePanLabel) view.findViewById(R.id.tvRefNoReceive);
		etNameReceive = (AutoCompleteTextView) view.findViewById(R.id.etNameReceive);
		etMobileNoReceive = (CodePanTextField) view.findViewById(R.id.etMobileNoReceive);
		etAddressReceive = (CodePanTextField) view.findViewById(R.id.etAddressReceive);
		btnCancelReceive = (CodePanButton) view.findViewById(R.id.btnCancelReceive);
		btnClaimReceive = (CodePanButton) view.findViewById(R.id.btnClaimReceive);
		btnPhotoReceive = (CodePanButton) view.findViewById(R.id.btnPhotoReceive);
		ivPhotoReceive = (ImageView) view.findViewById(R.id.ivPhotoReceive);
		btnCancelReceive.setOnClickListener(this);
		btnClaimReceive.setOnClickListener(this);
		btnPhotoReceive.setOnClickListener(this);
		if(remittance != null) {
			String date = CodePanUtils.getCalendarDate(remittance.smDate, true, true);
			String amount = "P" + nf.format(remittance.amount);
			tvDateReceive.setText(date);
			tvAmountReceive.setText(amount);
			tvTimeReceive.setText(remittance.smTime);
			tvRefNoReceive.setText(remittance.referenceNo);
		}
		CodePanUtils.requiredField(tvNameTitleReceive);
		String font = getString(R.string.helvetica_neue_light);
		Typeface typeface = TypefaceCache.get(main.getAssets(), font);
		etNameReceive.setTypeface(typeface);
		etNameReceive.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
				if(customerList != null) {
					customer = (CustomerObj) parent.getItemAtPosition(i);
					etMobileNoReceive.setText(customer.mobileNo);
					etAddressReceive.setText(customer.address);
					photo = customer.photo;
					if(photo != null) {
						final String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
								.getPath() + "/" + photo;
						CodePanUtils.displayImage(ivPhotoReceive, uri, R.drawable.ic_user);
					}
				}
			}
		});
		etNameReceive.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
				String text = cs.toString();
				if(customer != null) {
					if(text.isEmpty()) {
						etMobileNoReceive.setText(null);
						etAddressReceive.setText(null);
						withChanges = false;
						customer = null;
						photo = null;
					}
					else {
						withChanges = true;
					}
				}
			}

			@Override
			public void afterTextChanged(Editable e) {
			}
		});
		etAddressReceive.addTextChangedListener(this);
		etMobileNoReceive.addTextChangedListener(this);
		loadCustomers(db);
		return view;
	}

	public void setRemittance(RemittanceObj remittance) {
		this.remittance = remittance;
	}

	public void loadCustomers(final SQLiteAdapter db) {
		final Handler handler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				adapter = new CustomerAdapter(main, customerList);
				etNameReceive.setAdapter(adapter);
				return true;
			}
		});
		Thread bg = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					customerList = Data.loadCustomers(db);
					handler.obtainMessage().sendToTarget();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		bg.start();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnCancelReceive:
				manager.popBackStack();
				break;
			case R.id.btnClaimReceive:
				if(customer != null) {
					confirm(customer);
				}
				else {
					String name = etNameReceive.getText().toString().trim();
					String mobileNo = etMobileNoReceive.getText().toString().trim();
					String address = etAddressReceive.getText().toString().trim();
					if(!name.isEmpty()) {
						CustomerObj customer = SMPadalaLib.addCustomer(db, name, mobileNo, address, photo);
						confirm(customer);
					}
					else {
						SMPadalaLib.alertDialog(main, "Name Required", "Please input name.");
					}
				}
				break;
			case R.id.btnPhotoReceive:
				CameraFragment camera = new CameraFragment();
				camera.setOnUsePhotoCallback(new OnUsePhotoCallback() {
					@Override
					public void onUsePhoto(String fileName) {
						final String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
								.getPath() + "/" + fileName;
						CodePanUtils.displayImage(ivPhotoReceive, uri, R.drawable.ic_user);
						photo = fileName;
						if(customer != null) {
							withChanges = true;
						}
					}
				});
				transaction = manager.beginTransaction();
				transaction.add(R.id.rlMain, camera);
				transaction.addToBackStack(null);
				transaction.commit();
				break;
		}
	}

	public void confirm(final CustomerObj obj) {
		final AlertDialogFragment alert = new AlertDialogFragment();
		alert.setDialogTitle("Confirm Receiving");
		alert.setDialogMessage("Are you sure you want to receive this transaction?");
		alert.setPositiveButton("Yes", new OnClickListener() {
			CustomerObj customer;

			@Override
			public void onClick(View view) {
				manager.popBackStack();
				if(withChanges) {
					String name = etNameReceive.getText().toString().trim();
					String mobileNo = etMobileNoReceive.getText().toString().trim();
					String address = etAddressReceive.getText().toString().trim();
					customer = SMPadalaLib.editCustomer(db, name, mobileNo, address, photo, obj.ID);
				}
				else {
					customer = obj;
				}
				receive(db, customer);
			}
		});
		alert.setNegativeButton("No", new OnClickListener() {
			@Override
			public void onClick(View view) {
				manager.popBackStack();
			}
		});
		transaction = manager.beginTransaction();
		transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
				R.anim.fade_in, R.anim.fade_out);
		transaction.add(R.id.rlMain, alert);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public void receive(final SQLiteAdapter db, final CustomerObj customer) {
		final Handler handler = new Handler(new Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				switch(msg.what) {
					case Result.SUCCESS:
						remittance.receive = (ReceiveObj) msg.obj;
						remittance.isClaimed = true;
						CodePanUtils.alertToast(main, "Received by " + customer.name);
						if(receiveRemittanceCallback != null) {
							receiveRemittanceCallback.onReceiveRemittance(remittance);
						}
						manager.popBackStack();
						break;
					case Result.FAILED:
						CodePanUtils.alertToast(main, "Failed to receive. Please try again.");
						break;
				}
				return false;
			}
		});
		Thread bg = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ReceiveObj receive = SMPadalaLib.receive(db, customer, remittance.ID);
					handler.obtainMessage(receive != null ? Result.SUCCESS :
							Result.FAILED, receive).sendToTarget();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		bg.start();
	}

	public void setOnReceiveRemittanceCallback(OnReceiveRemittanceCallback receiveRemittanceCallback) {
		this.receiveRemittanceCallback = receiveRemittanceCallback;
	}

	@Override
	public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
	}

	@Override
	public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
		if(customer != null) {
			withChanges = true;
		}
	}

	@Override
	public void afterTextChanged(Editable editable) {
	}
}
