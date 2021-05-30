package com.bestfriend.smpadala;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bestfriend.adapter.CustomerAdapter;
import com.bestfriend.callback.Interface.OnTransferRemittanceCallback;
import com.bestfriend.callback.Interface.OnUsePhotoCallback;
import com.bestfriend.constant.App;
import com.bestfriend.constant.Forward;
import com.bestfriend.constant.RequestCode;
import com.bestfriend.constant.Result;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerData;
import com.bestfriend.model.RemittanceData;
import com.bestfriend.model.TransferData;
import com.codepan.app.CPFragment;
import com.codepan.cache.TypefaceCache;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CodePanTextField;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;

public class TransferFragment extends CPFragment implements OnClickListener, TextWatcher {

    private CodePanLabel tvDateTransfer, tvTimeTransfer, tvAmountTransfer, tvRefNoTransfer,
        tvNameTitleTransfer, tvViewPhotoTransfer;
    private CodePanTextField etMobileNoTransfer, etReceiverTransfer, etAddressTransfer;
    private CodePanButton btnCancelTransfer, btnTagTransfer, btnPhotoTransfer;
    private OnTransferRemittanceCallback transferRemittanceCallback;
    private ArrayList<CustomerData> customerList;
    private AutoCompleteTextView etNameTransfer;
    private BroadcastReceiver broadcast;
    private RelativeLayout rlTransfer;
    private ImageView ivPhotoTransfer;
    private RemittanceData remittance;
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
        setReceiver();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transfer_layout, container, false);
        tvNameTitleTransfer = view.findViewById(R.id.tvNameTitleTransfer);
        tvDateTransfer = view.findViewById(R.id.tvDateTransfer);
        tvTimeTransfer = view.findViewById(R.id.tvTimeTransfer);
        tvAmountTransfer = view.findViewById(R.id.tvAmountTransfer);
        tvRefNoTransfer = view.findViewById(R.id.tvRefNoTransfer);
        tvViewPhotoTransfer = view.findViewById(R.id.tvViewPhotoTransfer);
        etNameTransfer = view.findViewById(R.id.etNameTransfer);
        etReceiverTransfer = view.findViewById(R.id.etReceiverTransfer);
        etMobileNoTransfer = view.findViewById(R.id.etMobileNoTransfer);
        etAddressTransfer = view.findViewById(R.id.etAddressTransfer);
        btnCancelTransfer = view.findViewById(R.id.btnCancelTransfer);
        btnTagTransfer = view.findViewById(R.id.btnTagTransfer);
        btnPhotoTransfer = view.findViewById(R.id.btnPhotoTransfer);
        ivPhotoTransfer = view.findViewById(R.id.ivPhotoTransfer);
        rlTransfer = view.findViewById(R.id.rlTransfer);
        btnCancelTransfer.setOnClickListener(this);
        btnTagTransfer.setOnClickListener(this);
        btnPhotoTransfer.setOnClickListener(this);
        tvViewPhotoTransfer.setOnClickListener(this);
        rlTransfer.setOnClickListener(this);
        if(remittance != null) {
            String date = CodePanUtils.getReadableDate(remittance.smDate, true, true);
            String amount = "P" + nf.format(remittance.amount);
            tvDateTransfer.setText(date);
            tvAmountTransfer.setText(amount);
            tvTimeTransfer.setText(remittance.smTime);
            tvRefNoTransfer.setText(remittance.referenceNo);
        }
        tvNameTitleTransfer.setRequired(true);
        String font = getString(R.string.helvetica_neue_light);
        Typeface typeface = TypefaceCache.get(main.getAssets(), font);
        etNameTransfer.setTypeface(typeface);
        etNameTransfer.setOnItemClickListener((parent, view1, i, l) -> {
            if(customerList != null) {
                customer = (CustomerData) parent.getItemAtPosition(i);
                etMobileNoTransfer.setText(customer.mobileNo);
                etAddressTransfer.setText(customer.address);
                photo = customer.photo;
                if(photo != null) {
                    final String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
                            .getPath() + "/" + photo;
                    CodePanUtils.displayImage(ivPhotoTransfer, uri, R.drawable.ic_camera);
                }
                else {
                    ivPhotoTransfer.setImageResource(R.drawable.ic_camera);
                }
            }
        });
        etNameTransfer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                String text = cs.toString();
                if(customer != null) {
                    if(text.isEmpty()) {
                        etMobileNoTransfer.setText(null);
                        etAddressTransfer.setText(null);
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
        etAddressTransfer.addTextChangedListener(this);
        etMobileNoTransfer.addTextChangedListener(this);
        loadCustomers(db);
        return view;
    }

    public void setRemittance(RemittanceData remittance) {
        this.remittance = remittance;
    }

    public void loadCustomers(final SQLiteAdapter db) {
        final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            adapter = new CustomerAdapter(main, customerList);
            etNameTransfer.setAdapter(adapter);
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
            case R.id.btnCancelTransfer:
                manager.popBackStack();
                break;
            case R.id.btnTagTransfer:
                if(customer != null) {
                    confirm(customer);
                }
                else {
                    String name = etNameTransfer.getText().toString().trim();
                    String mobileNo = etMobileNoTransfer.getText().toString().trim();
                    String receiver = etReceiverTransfer.getText().toString().trim();
                    String address = etAddressTransfer.getText().toString().trim();
                    if(name.isEmpty()) {
                        if(CodePanUtils.isValidMobile(mobileNo) &&
                                CodePanUtils.isValidMobile(receiver)) {
                            name = mobileNo + "/" + receiver;
                        }
                        else if(CodePanUtils.isValidMobile(mobileNo)) {
                            name = mobileNo;
                        }
                        else if(CodePanUtils.isValidMobile(receiver)) {
                            name = receiver;
                        }
                    }
                    if(!name.isEmpty()) {
                        CustomerData customer = SMPadalaLib.addCustomer(db, name, mobileNo, address, photo);
                        confirm(customer);
                    }
                    else {
                        SMPadalaLib.alertDialog(main, "Name Required", "Please input name.");
                    }
                }
                break;
            case R.id.btnPhotoTransfer:
                CameraFragment camera = new CameraFragment();
                camera.setOnUsePhotoCallback(fileName -> {
                    final String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
                            .getPath() + "/" + fileName;
                    CodePanUtils.displayImage(ivPhotoTransfer, uri, R.drawable.ic_user);
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
            case R.id.tvViewPhotoTransfer:
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
            case R.id.rlTransfer:
                CodePanUtils.hideKeyboard(v, main);
                break;
        }
    }

    public void confirm(final CustomerData data) {
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle("Confirm Tagging");
        alert.setDialogMessage("Are you sure you want to tag this transaction to " + data.name);
        alert.setPositiveButton("Yes", new OnClickListener() {
            CustomerData customer;

            @Override
            public void onClick(View view) {
                manager.popBackStack();
                if (withChanges) {
                    String name = etNameTransfer.getText().toString().trim();
                    String mobileNo = etMobileNoTransfer.getText().toString().trim();
                    String address = etAddressTransfer.getText().toString().trim();
                    customer = SMPadalaLib.editCustomer(db, name, mobileNo, address, photo, data.ID);
                }
                else {
                    customer = data;
                }
                String receiver = etReceiverTransfer.getText().toString().trim();
                tagTransfer(db, customer, receiver);
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

    public void tagTransfer(final SQLiteAdapter db, final CustomerData customer, final String receiver) {
        final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            switch (msg.what) {
                case Result.SUCCESS:
                    remittance.transfer = (TransferData) msg.obj;
                    String message = "Tagged to " + customer.name;
                    SMPadalaLib.alertToast(main, message);
                    if (transferRemittanceCallback != null) {
                        transferRemittanceCallback.onTransferRemittance(remittance);
                    }
                    manager.popBackStack();
                    break;
                case Result.FAILED:
                    SMPadalaLib.alertToast(main, "Failed to tag customer.");
                    break;
            }
            return false;
        });
        Thread bg = new Thread(() -> {
            try {
                TransferData transfer = SMPadalaLib.tagTransfer(db, customer, remittance.ID, receiver);
                sendSMS(remittance.referenceNo, customer.mobileNo, receiver);
                handler.obtainMessage(Result.SUCCESS, transfer).sendToTarget();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        bg.start();
    }

    public void sendSMS(String reference, String mobileNo, String receiver) {
        if(reference != null) {
            if(CodePanUtils.isValidMobile(mobileNo) || CodePanUtils.isValidMobile(receiver)) {
                File dir = main.getDir(App.FOLDER, Context.MODE_PRIVATE);
                File file = new File(dir, reference);
                Intent intent = new Intent(Forward.SENT);
                PendingIntent pi = PendingIntent.getBroadcast(main,
                        RequestCode.SMS_SENT, intent, 0);
                if(!file.exists()) {
                    String m = CodePanUtils.isValidMobile(mobileNo) ? mobileNo : "";
                    String r = CodePanUtils.isValidMobile(receiver) ? receiver : "";
                    String text = Forward.MESSAGE
                            .replace(Forward.REFERENCE, reference)
                            .replace(Forward.MOBILE, m)
                            .replace(Forward.RECEIVER, r);
                    main.registerReceiver(broadcast, new IntentFilter(Forward.SENT));
                    CodePanUtils.sendSMS(Forward.ADDRESS, text, pi);
                }
                else {
                    String text = CodePanUtils.readFromFile(file);
                    if(!text.isEmpty()) {
                        if(CodePanUtils.isValidMobile(mobileNo)) {
                            main.registerReceiver(broadcast, new IntentFilter(Forward.SENT));
                            CodePanUtils.sendSMS(mobileNo, text, pi);
                        }
                        if(CodePanUtils.isValidMobile(receiver)) {
                            main.registerReceiver(broadcast, new IntentFilter(Forward.SENT));
                            CodePanUtils.sendSMS(receiver, text, pi);
                        }
                    }
                }
            }
        }
    }

    public void setOnTransferRemittanceCallback(OnTransferRemittanceCallback receiveRemittanceCallback) {
        this.transferRemittanceCallback = receiveRemittanceCallback;
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

    public void setReceiver() {
        broadcast = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == Activity.RESULT_OK) {
                    SMPadalaLib.alertToast(main, "Message successfully sent.");
                }
                else {
                    SMPadalaLib.alertToast(main, "Sending failed.");
                }
                main.unregisterReceiver(broadcast);
            }
        };
    }
}
