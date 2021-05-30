package com.bestfriend.smpadala;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bestfriend.callback.Interface.OnSaveCustomerCallback;
import com.bestfriend.constant.App;
import com.bestfriend.constant.Result;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerData;
import com.codepan.app.CPFragment;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CodePanTextField;

public class AddCustomerFragment extends CPFragment implements View.OnClickListener {

    private CodePanTextField etNameAddCustomer, etMobileNoAddCustomer, etAddressAddCustomer;
    private CodePanLabel tvTitleAddCustomer, tvNameTitleAddCustomer, tvViewPhotoAddCustomer;
    private CodePanButton btnPhotoAddCustomer, btnCancelAddCustomer, btnSaveAddCustomer;
    private OnSaveCustomerCallback saveCustomerCallback;
    private ImageView ivPhotoAddCustomer;
    private CustomerData customer;
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
        View view = inflater.inflate(R.layout.add_customer_layout, container, false);
        tvTitleAddCustomer = view.findViewById(R.id.tvTitleAddCustomer);
        tvViewPhotoAddCustomer = view.findViewById(R.id.tvViewPhotoAddCustomer);
        tvNameTitleAddCustomer = view.findViewById(R.id.tvNameTitleAddCustomer);
        etNameAddCustomer = view.findViewById(R.id.etNameAddCustomer);
        etMobileNoAddCustomer = view.findViewById(R.id.etMobileNoAddCustomer);
        etAddressAddCustomer = view.findViewById(R.id.etAddressAddCustomer);
        btnCancelAddCustomer = view.findViewById(R.id.btnCancelAddCustomer);
        btnSaveAddCustomer = view.findViewById(R.id.btnSaveAddCustomer);
        btnPhotoAddCustomer = view.findViewById(R.id.btnPhotoAddCustomer);
        ivPhotoAddCustomer = view.findViewById(R.id.ivPhotoAddCustomer);
        tvViewPhotoAddCustomer.setOnClickListener(this);
        btnCancelAddCustomer.setOnClickListener(this);
        btnPhotoAddCustomer.setOnClickListener(this);
        btnSaveAddCustomer.setOnClickListener(this);
        tvNameTitleAddCustomer.setRequired(true);
        if(customer != null) {
            etNameAddCustomer.setText(customer.name);
            etMobileNoAddCustomer.setText(customer.mobileNo);
            etAddressAddCustomer.setText(customer.address);
            if(customer.photo != null) {
                String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
                        .getPath() + "/" + customer.photo;
                CodePanUtils.displayImage(ivPhotoAddCustomer, uri, R.drawable.ic_camera);
                tvViewPhotoAddCustomer.setVisibility(View.VISIBLE);
                photo = customer.photo;
            }
            else {
                tvViewPhotoAddCustomer.setVisibility(View.GONE);
            }
            tvTitleAddCustomer.setText(R.string.edit_customer);
        }
        else {
            tvViewPhotoAddCustomer.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnPhotoAddCustomer:
                CameraFragment camera = new CameraFragment();
                camera.setOnUsePhotoCallback(fileName -> {
                    final String uri = "file://" + main.getDir(App.FOLDER, Context.MODE_PRIVATE)
                            .getPath() + "/" + fileName;
                    CodePanUtils.displayImage(ivPhotoAddCustomer, uri, R.drawable.ic_camera);
                    tvViewPhotoAddCustomer.setVisibility(View.VISIBLE);
                    photo = fileName;
                });
                transaction = manager.beginTransaction();
                transaction.add(R.id.rlMain, camera);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.btnCancelAddCustomer:
                manager.popBackStack();
                break;
            case R.id.tvViewPhotoAddCustomer:
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
            case R.id.btnSaveAddCustomer:
                String name = etNameAddCustomer.getText().toString().trim();
                String mobileNo = etMobileNoAddCustomer.getText().toString().trim();
                String address = etAddressAddCustomer.getText().toString().trim();
                if(!name.isEmpty()) {
                    saveCustomer(db, name, mobileNo, address);
                }
                else {
                    SMPadalaLib.alertDialog(main, "Name Required", "Please input name.");
                }
                break;
        }
    }

    public void saveCustomer(final SQLiteAdapter db, final String name, final String mobileNo, final String address) {
        Thread bg = new Thread(() -> {
            try {
                if (customer != null) {
                    customer = SMPadalaLib.editCustomer(db, name, mobileNo, address, photo, customer.ID);
                }
                else {
                    customer = SMPadalaLib.addCustomer(db, name, mobileNo, address, photo);
                }
                handler.obtainMessage(customer.ID != null ? Result.SUCCESS : Result.FAILED).sendToTarget();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        bg.start();
    }

    private final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
        switch (msg.what) {
            case Result.SUCCESS:
                SMPadalaLib.alertToast(main, "Customer has been successfully saved.");
                break;
            case Result.FAILED:
                SMPadalaLib.alertToast(main, "Failed to save customer.");
                break;
        }
        manager.popBackStack();
        if (saveCustomerCallback != null) {
            saveCustomerCallback.onSaveCustomer(customer);
        }
        return true;
    });

    public void setCustomer(CustomerData customer) {
        this.customer = customer;
    }

    public void setOnSaveCustomerCallback(OnSaveCustomerCallback saveCustomerCallback) {
        this.saveCustomerCallback = saveCustomerCallback;
    }
}
