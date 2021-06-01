package com.bestfriend.smpadala;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.bestfriend.adapter.CustomerAdapter;
import com.bestfriend.callback.Interface.OnSelectCustomerCallback;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerData;
import com.codepan.app.CPFragment;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CodePanTextField;

import java.util.ArrayList;

public class CustomerFragment extends CPFragment implements OnClickListener {

	private CodePanButton btnCancelCustomer, btnAddCustomer;
	private OnSelectCustomerCallback selectCustomerCallback;
	private ArrayList<CustomerData> customerList;
	private CodePanTextField etSearchCustomer;
	private CodePanLabel tvTitleCustomer;
	private CustomerAdapter adapter;
	private ListView lvCustomer;
	private MainActivity main;
	private SQLiteAdapter db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.disableBackPressed();
		main = (MainActivity) getActivity();
		manager = main.getSupportFragmentManager();
		db = main.getDatabase();
		db.openConnection();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.customer_layout, container, false);
		tvTitleCustomer = view.findViewById(R.id.tvTitleCustomer);
		etSearchCustomer = view.findViewById(R.id.etSearchCustomer);
		btnCancelCustomer = view.findViewById(R.id.btnCancelCustomer);
		btnAddCustomer = view.findViewById(R.id.btnAddCustomer);
		lvCustomer = view.findViewById(R.id.lvCustomer);
		btnCancelCustomer.setOnClickListener(this);
		btnAddCustomer.setOnClickListener(this);
		int count = SMPadalaLib.getNoOfCustomers(db);
		String customers = main.getResources().getString(R.string.customers);
		String title = customers + " (" + count + ")";
		tvTitleCustomer.setText(title);
		etSearchCustomer.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
				if(adapter != null) {
					adapter.getFilter().filter(cs);
				}
			}

			@Override
			public void afterTextChanged(Editable e) {
			}
		});
		lvCustomer.setOnItemClickListener((adapterView, view12, i, l) -> {
			CodePanUtils.hideKeyboard(view12, main);
			CustomerData customer = customerList.get(i);
			if (selectCustomerCallback != null) {
				selectCustomerCallback.onSelectCustomer(customer);
				manager.popBackStack();
			}
			else {
				AddCustomerFragment add = new AddCustomerFragment();
				add.setCustomer(customer);
				add.setOnSaveCustomerCallback(customer1 -> {
					customerList.set(i, customer1);
					lvCustomer.invalidate();
					adapter.notifyDataSetChanged();
				});
				transaction = manager.beginTransaction();
				transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
						R.anim.fade_in, R.anim.fade_out);
				transaction.add(R.id.rlMain, add);
				transaction.addToBackStack(null);
				transaction.commit();
			}
		});
		lvCustomer.setOnItemLongClickListener((parent, view1, i, id) -> {
			CustomerData customer = customerList.get(i);
			confirmDelete(customer);
			return true;
		});
		loadCustomers(db);
		return view;
	}

	public void loadCustomers(final SQLiteAdapter db) {
		final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
			adapter = new CustomerAdapter(main, customerList);
			lvCustomer.setAdapter(adapter);
			String search = etSearchCustomer.getText().toString();
			if(!search.isEmpty()) {
				adapter.getFilter().filter(search);
			}
			return true;
		});
		Thread bg = new Thread(() -> {
			try {
				customerList = Data.loadCustomers(db);
				handler.obtainMessage().sendToTarget();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});
		bg.start();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnCancelCustomer:
				manager.popBackStack();
				break;
			case R.id.btnAddCustomer:
				AddCustomerFragment add = new AddCustomerFragment();
				add.setOnSaveCustomerCallback(customer -> loadCustomers(db));
				transaction = manager.beginTransaction();
				transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
					R.anim.fade_in, R.anim.fade_out);
				transaction.add(R.id.rlMain, add);
				transaction.addToBackStack(null);
				transaction.commit();
				break;
		}
	}

	public void confirmDelete(final CustomerData customer) {
		final AlertDialogFragment alert = new AlertDialogFragment();
		alert.setDialogTitle("Delete Customer");
		alert.setDialogMessage("Are you sure you want to delete this customer?");
		alert.setPositiveButton("Yes", view -> {
			manager.popBackStack();
			boolean result = SMPadalaLib.deleteCustomer(db, customer.ID);
			if (result) {
				customerList.remove(customer);
				lvCustomer.invalidate();
				adapter.notifyDataSetChanged();
				SMPadalaLib.alertToast(main, "Customer has been successfully deleted.");
			}
		});
		alert.setNegativeButton("No", view -> manager.popBackStack());
		transaction = manager.beginTransaction();
		transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
				R.anim.fade_in, R.anim.fade_out);
		transaction.add(R.id.rlMain, alert);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	public void setOnSelectCustomerCallback(OnSelectCustomerCallback selectCustomerCallback) {
		this.selectCustomerCallback = selectCustomerCallback;
	}
}
