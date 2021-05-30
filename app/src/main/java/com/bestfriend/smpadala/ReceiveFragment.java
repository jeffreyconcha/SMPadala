package com.bestfriend.smpadala;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bestfriend.adapter.CustomerAdapter;
import com.bestfriend.callback.Interface.OnReceiveRemittanceCallback;
import com.bestfriend.constant.App;
import com.bestfriend.constant.Result;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerData;
import com.bestfriend.model.ReceiveData;
import com.bestfriend.model.RemittanceData;
import com.codepan.app.CPFragment;
import com.codepan.cache.TypefaceCache;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CodePanTextField;

import java.text.NumberFormat;
import java.util.ArrayList;

public class ReceiveFragment extends CPFragment implements OnClickListener, TextWatcher {

    private CodePanLabel tvDateReceive, tvTimeReceive, tvAmountReceive, tvRefNoReceive,
        tvNameTitleReceive, tvViewPhotoReceive;
    private CodePanButton btnCancelReceive, btnClaimReceive, btnPhotoReceive;
    private OnReceiveRemittanceCallback receiveRemittanceCallback;
    private CodePanTextField etMobileNoReceive, etAddressReceive;
    private RelativeLayout rlClaimedReceive, rlReceive;
    private ArrayList<CustomerData> customerList;
    private AutoCompleteTextView etNameReceive;
    private CheckBox cbClaimedReceive;
    private RemittanceData remittance;
    private ImageView ivPhotoReceive;
    private CustomerAdapter adapter;
    private CustomerData customer;
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
        rlClaimedReceive = view.findViewById(R.id.rlClaimedReceive);
        cbClaimedReceive = view.findViewById(R.id.cbClaimedReceive);
        tvNameTitleReceive = view.findViewById(R.id.tvNameTitleReceive);
        tvDateReceive = view.findViewById(R.id.tvDateReceive);
        tvTimeReceive = view.findViewById(R.id.tvTimeReceive);
        tvAmountReceive = view.findViewById(R.id.tvAmountReceive);
        tvRefNoReceive = view.findViewById(R.id.tvRefNoReceive);
        tvViewPhotoReceive = view.findViewById(R.id.tvViewPhotoReceive);
        etNameReceive = view.findViewById(R.id.etNameReceive);
        etMobileNoReceive = view.findViewById(R.id.etMobileNoReceive);
        etAddressReceive = view.findViewById(R.id.etAddressReceive);
        btnCancelReceive = view.findViewById(R.id.btnCancelReceive);
        btnClaimReceive = view.findViewById(R.id.btnClaimReceive);
        btnPhotoReceive = view.findViewById(R.id.btnPhotoReceive);
        ivPhotoReceive = view.findViewById(R.id.ivPhotoReceive);
        rlReceive = view.findViewById(R.id.rlReceive);
        btnCancelReceive.setOnClickListener(this);
        btnClaimReceive.setOnClickListener(this);
        btnPhotoReceive.setOnClickListener(this);
        rlClaimedReceive.setOnClickListener(this);
        tvViewPhotoReceive.setOnClickListener(this);
        rlReceive.setOnClickListener(this);
        if(remittance != null) {
            String date = CodePanUtils.getReadableDate(remittance.smDate, true, true);
            String amount = "P" + nf.format(remittance.amount);
            tvDateReceive.setText(date);
            tvAmountReceive.setText(amount);
            tvTimeReceive.setText(remittance.smTime);
            tvRefNoReceive.setText(remittance.referenceNo);
        }
        tvNameTitleReceive.setRequired(true);
        String font = getString(R.string.helvetica_neue_light);
        Typeface typeface = TypefaceCache.get(main.getAssets(), font);
        etNameReceive.setTypeface(typeface);
        etNameReceive.setOnItemClickListener((parent, view12, i, l) -> {
            if(customerList != null) {
                customer = (CustomerData) parent.getItemAtPosition(i);
                etMobileNoReceive.setText(customer.mobileNo);
                etAddressReceive.setText(customer.address);
                photo = customer.photo;
                if(photo != null) {
                    final String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
                            .getPath() + "/" + photo;
                    CodePanUtils.displayImage(ivPhotoReceive, uri, R.drawable.ic_camera);
                }
                else {
                    ivPhotoReceive.setImageResource(R.drawable.ic_camera);
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
        cbClaimedReceive.setOnCheckedChangeListener((view1, isChecked) -> {
            int action = isChecked ? R.string.receive : R.string.mark;
            btnClaimReceive.setText(action);
        });
        loadCustomers(db);
        return view;
    }

    public void setRemittance(RemittanceData remittance) {
        this.remittance = remittance;
    }

    public void loadCustomers(final SQLiteAdapter db) {
        final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            adapter = new CustomerAdapter(main, customerList);
            etNameReceive.setAdapter(adapter);
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
                        CustomerData customer = SMPadalaLib.addCustomer(db, name, mobileNo, address, photo);
                        confirm(customer);
                    }
                    else {
                        SMPadalaLib.alertDialog(main, "Name Required", "Please input name.");
                    }
                }
                break;
            case R.id.btnPhotoReceive:
                CameraFragment camera = new CameraFragment();
                camera.setOnUsePhotoCallback(fileName -> {
                    final String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
                            .getPath() + "/" + fileName;
                    CodePanUtils.displayImage(ivPhotoReceive, uri, R.drawable.ic_user);
                    photo = fileName;
                    if(customer != null) {
                        withChanges = true;
                    }
                });
                transaction = manager.beginTransaction();
                transaction.add(R.id.rlMain, camera);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.tvViewPhotoReceive:
                if(photo != null) {
                    ImagePreviewFragment preview = new ImagePreviewFragment();
                    preview.setPhoto(photo);
                    transaction = manager.beginTransaction();
                    transaction.add(R.id.rlMain, preview);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                else {
                    SMPadalaLib.alertToast(main, "No photo to be viewed");
                }
                break;
            case R.id.rlClaimedReceive:
                cbClaimedReceive.setChecked(!cbClaimedReceive.isChecked());
                break;
            case R.id.rlReceive:
                CodePanUtils.hideKeyboard(v, main);
                break;
        }
    }

    public void confirm(final CustomerData data) {
        String title = cbClaimedReceive.isChecked() ? "Confirm Receiving" : "Mark Transaction";
        String message = cbClaimedReceive.isChecked() ? "Are you sure you want to receive this " +
            "transaction?" : "Marking a transaction will only tag the customer but will " +
            "still be marked as pending. Are you sure you want to mark this transaction?";
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle(title);
        alert.setDialogMessage(message);
        alert.setPositiveButton("Yes", new OnClickListener() {
            CustomerData customer;

            @Override
            public void onClick(View view) {
                manager.popBackStack();
                if(withChanges) {
                    String name = etNameReceive.getText().toString().trim();
                    String mobileNo = etMobileNoReceive.getText().toString().trim();
                    String address = etAddressReceive.getText().toString().trim();
                    customer = SMPadalaLib.editCustomer(db, name, mobileNo, address, photo, data.ID);
                }
                else {
                    customer = data;
                }
                receive(db, customer);
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

    public void receive(final SQLiteAdapter db, final CustomerData customer) {
        final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            String message = null;
            switch (msg.what) {
                case Result.SUCCESS:
                    remittance.receive = (ReceiveData) msg.obj;
                    remittance.isClaimed = cbClaimedReceive.isChecked();
                    remittance.isMarked = !cbClaimedReceive.isChecked();
                    message = cbClaimedReceive.isChecked() ? "Received by " + customer.name :
                        "Mark successful.";
                    SMPadalaLib.alertToast(main, message);
                    if (receiveRemittanceCallback != null) {
                        receiveRemittanceCallback.onReceiveRemittance(remittance);
                    }
                    manager.popBackStack();
                    break;
                case Result.FAILED:
                    message = cbClaimedReceive.isChecked() ? "Failed to receive transaction. " +
                            "Please try again." : "Failed to mark transaction. Please try again.";
                    SMPadalaLib.alertToast(main, message);
                    break;
            }
            return false;
        });
        Thread bg = new Thread(() -> {
            try {
                ReceiveData receive = SMPadalaLib.receive(db, customer, remittance.ID,
                    cbClaimedReceive.isChecked());
                handler.obtainMessage(receive.ID != null ? Result.SUCCESS :
                        Result.FAILED, receive).sendToTarget();
            }
            catch(Exception e) {
                e.printStackTrace();
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
