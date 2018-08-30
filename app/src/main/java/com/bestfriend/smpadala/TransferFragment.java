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
import android.widget.RelativeLayout;

import com.bestfriend.adapter.CustomerAdapter;
import com.bestfriend.callback.Interface.OnTransferRemittanceCallback;
import com.bestfriend.callback.Interface.OnUsePhotoCallback;
import com.bestfriend.constant.App;
import com.bestfriend.constant.Result;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.TransferObj;
import com.bestfriend.model.RemittanceObj;
import com.codepan.cache.TypefaceCache;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CodePanTextField;

import java.text.NumberFormat;
import java.util.ArrayList;

public class TransferFragment extends Fragment implements OnClickListener, TextWatcher {

    private CodePanLabel tvDateTransfer, tvTimeTransfer, tvAmountTransfer, tvRefNoTransfer,
            tvNameTitleTransfer, tvViewPhotoTransfer;
    private CodePanTextField etMobileNoTransfer, etReceiverTransfer, etAddressTransfer;
    private CodePanButton btnCancelTransfer, btnTagTransfer, btnPhotoTransfer;
    private OnTransferRemittanceCallback transferRemittanceCallback;
    private ArrayList<CustomerObj> customerList;
    private AutoCompleteTextView etNameTransfer;
    private FragmentTransaction transaction;
    private RelativeLayout rlTransfer;
    private ImageView ivPhotoTransfer;
    private RemittanceObj remittance;
    private CustomerAdapter adapter;
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
            String date = CodePanUtils.getCalendarDate(remittance.smDate, true, true);
            String amount = "P" + nf.format(remittance.amount);
            tvDateTransfer.setText(date);
            tvAmountTransfer.setText(amount);
            tvTimeTransfer.setText(remittance.smTime);
            tvRefNoTransfer.setText(remittance.referenceNo);
        }
        CodePanUtils.requiredField(tvNameTitleTransfer);
        String font = getString(R.string.helvetica_neue_light);
        Typeface typeface = TypefaceCache.get(main.getAssets(), font);
        etNameTransfer.setTypeface(typeface);
        etNameTransfer.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                if(customerList != null) {
                    customer = (CustomerObj) parent.getItemAtPosition(i);
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

    public void setRemittance(RemittanceObj remittance) {
        this.remittance = remittance;
    }

    public void loadCustomers(final SQLiteAdapter db) {
        final Handler handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                adapter = new CustomerAdapter(main, customerList);
                etNameTransfer.setAdapter(adapter);
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
                    String address = etAddressTransfer.getText().toString().trim();
                    if(!name.isEmpty()) {
                        CustomerObj customer = SMPadalaLib.addCustomer(db, name, mobileNo, address, photo);
                        confirm(customer);
                    }
                    else {
                        SMPadalaLib.alertDialog(main, "Name Required", "Please input name.");
                    }
                }
                break;
            case R.id.btnPhotoTransfer:
                CameraFragment camera = new CameraFragment();
                camera.setOnUsePhotoCallback(new OnUsePhotoCallback() {
                    @Override
                    public void onUsePhoto(String fileName) {
                        final String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
                                                               .getPath() + "/" + fileName;
                        CodePanUtils.displayImage(ivPhotoTransfer, uri, R.drawable.ic_user);
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
                    CodePanUtils.alertToast(main, "No photo to be viewed");
                }
                break;
            case R.id.rlTransfer:
                CodePanUtils.hideKeyboard(v, main);
                break;
        }
    }

    public void confirm(final CustomerObj obj) {
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle("Confirm Tagging");
        alert.setDialogMessage("Are you sure you want to tag this transaction to " + obj.name);
        alert.setPositiveButton("Yes", new OnClickListener() {
            CustomerObj customer;

            @Override
            public void onClick(View view) {
                manager.popBackStack();
                if(withChanges) {
                    String name = etNameTransfer.getText().toString().trim();
                    String mobileNo = etMobileNoTransfer.getText().toString().trim();
                    String address = etAddressTransfer.getText().toString().trim();
                    customer = SMPadalaLib.editCustomer(db, name, mobileNo, address, photo, obj.ID);
                }
                else {
                    customer = obj;
                }
                String receiver = etReceiverTransfer.getText().toString().trim();
                tagTransfer(db, customer, receiver);
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

    public void tagTransfer(final SQLiteAdapter db, final CustomerObj customer, final String receiver) {
        final Handler handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch(msg.what) {
                    case Result.SUCCESS:
                        remittance.transfer = (TransferObj) msg.obj;
                        String message = "Tagged to " + customer.name;
                        CodePanUtils.alertToast(main, message);
                        if(transferRemittanceCallback != null) {
                            transferRemittanceCallback.onTransferRemittance(remittance);
                        }
                        manager.popBackStack();
                        break;
                    case Result.FAILED:
                        CodePanUtils.alertToast(main, "Failed to tag customer.");
                        break;
                }
                return false;
            }
        });
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    TransferObj transfer = SMPadalaLib.tagTransfer(db, customer, remittance.ID, receiver);
                    handler.obtainMessage(transfer != null ? Result.SUCCESS :
                                                  Result.FAILED, transfer).sendToTarget();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bg.start();
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
}
