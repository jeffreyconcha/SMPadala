package com.bestfriend.smpadala;

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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.bestfriend.adapter.CustomerAdapter;
import com.bestfriend.callback.Interface.OnSaveCustomerCallback;
import com.bestfriend.callback.Interface.OnSelectCustomerCallback;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerObj;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanTextField;

import java.util.ArrayList;

public class CustomerFragment extends Fragment implements OnClickListener {

	private CodePanButton btnCancelCustomer, btnAddCustomer;
	private OnSelectCustomerCallback selectCustomerCallback;
	private ArrayList<CustomerObj> customerList;
	private CodePanTextField etSearchCustomer;
	private FragmentTransaction transaction;
	private FragmentManager manager;
	private CustomerAdapter adapter;
	private ListView lvCustomer;
	private MainActivity main;
	private SQLiteAdapter db;

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
		View view = inflater.inflate(R.layout.customer_layout, container, false);
		etSearchCustomer = view.findViewById(R.id.etSearchCustomer);
		btnCancelCustomer = view.findViewById(R.id.btnCancelCustomer);
		btnAddCustomer = view.findViewById(R.id.btnAddCustomer);
		lvCustomer = view.findViewById(R.id.lvCustomer);
		btnCancelCustomer.setOnClickListener(this);
		btnAddCustomer.setOnClickListener(this);
		etSearchCustomer.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
				adapter.getFilter().filter(cs);
			}

			@Override
			public void afterTextChanged(Editable e) {
			}
		});
		lvCustomer.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(final AdapterView<?> adapterView, View view, final int i, long l) {
				CustomerObj customer = customerList.get(i);
				if(selectCustomerCallback != null) {
					selectCustomerCallback.onSelectCustomer(customer);
					manager.popBackStack();
				}
				else {
					AddCustomerFragment add = new AddCustomerFragment();
					add.setCustomer(customer);
					add.setOnSaveCustomerCallback(new OnSaveCustomerCallback() {
						@Override
						public void onSaveCustomer(CustomerObj customer) {
							customerList.set(i, customer);
							lvCustomer.invalidate();
							adapter.notifyDataSetChanged();
						}
					});
					transaction = manager.beginTransaction();
					transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
							R.anim.fade_in, R.anim.fade_out);
					transaction.add(R.id.rlMain, add);
					transaction.addToBackStack(null);
					transaction.commit();
				}
			}
		});
		lvCustomer.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
				CustomerObj customer = customerList.get(i);
				confirmDelete(customer);
				return true;
			}
		});
		loadCustomers(db);
		return view;
	}

	public void loadCustomers(final SQLiteAdapter db) {
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

	Handler handler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			adapter = new CustomerAdapter(main, customerList);
			lvCustomer.setAdapter(adapter);
			return true;
		}
	});

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.btnCancelCustomer:
				manager.popBackStack();
				break;
			case R.id.btnAddCustomer:
				AddCustomerFragment add = new AddCustomerFragment();
				add.setOnSaveCustomerCallback(new OnSaveCustomerCallback() {
					@Override
					public void onSaveCustomer(CustomerObj customer) {
						loadCustomers(db);
					}
				});
				transaction = manager.beginTransaction();
				transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
						R.anim.fade_in, R.anim.fade_out);
				transaction.add(R.id.rlMain, add);
				transaction.addToBackStack(null);
				transaction.commit();
				break;
		}
	}

	public void confirmDelete(final CustomerObj customer) {
		final AlertDialogFragment alert = new AlertDialogFragment();
		alert.setDialogTitle("Delete Customer");
		alert.setDialogMessage("Are you sure you want to delete this customer?");
		alert.setPositiveButton("Yes", new OnClickListener() {
			@Override
			public void onClick(View view) {
				manager.popBackStack();
				boolean result = SMPadalaLib.deleteCustomer(db, customer.ID);
				if(result) {
					customerList.remove(customer);
					lvCustomer.invalidate();
					adapter.notifyDataSetChanged();
					CodePanUtils.alertToast(main, "Customer has been successfully deleted.");
				}
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

	public void setOnSelectCustomerCallback(OnSelectCustomerCallback selectCustomerCallback) {
		this.selectCustomerCallback = selectCustomerCallback;
	}
}
