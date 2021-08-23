package com.bestfriend.smpadala;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bestfriend.adapter.RemittanceAdapter;
import com.bestfriend.cache.SQLiteCache;
import com.bestfriend.constant.App;
import com.bestfriend.constant.DialogTag;
import com.bestfriend.constant.Key;
import com.bestfriend.constant.Notification;
import com.bestfriend.constant.ProcessName;
import com.bestfriend.constant.RemittanceStatus;
import com.bestfriend.constant.RemittanceType;
import com.bestfriend.constant.RequestCode;
import com.bestfriend.constant.Result;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerData;
import com.bestfriend.model.ReceiveData;
import com.bestfriend.model.RemittanceData;
import com.bestfriend.model.TransferData;
import com.codepan.app.CPFragmentActivity;
import com.codepan.callback.Interface.OnInitializeCallback;
import com.codepan.database.SQLiteAdapter;
import com.codepan.permission.PermissionEvents;
import com.codepan.permission.PermissionHandler;
import com.codepan.permission.PermissionType;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CodePanTextField;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends CPFragmentActivity implements OnInitializeCallback,
    OnClickListener, AbsListView.OnScrollListener, PermissionEvents {

    private final int LIMIT = 200;
    private final long IDLE_TIME = 500;

    private CodePanButton btnMenuMain, btnShowFilterMain, btnDateMain, btnTypeMain,
        btnStatusMain, btnCustomerMain, btnFilterMain, btnClearMain;
    private LinearLayout llMenuMain, llCustomersMain, llDataAnalyticsMain,
        llBackupMain, llRestoreBackupMain, llDailySummaryMain;
    private int visibleItem, totalItem, firstVisible;
    private ArrayList<RemittanceData> remittanceList;
    private CodePanLabel tvDateMain, tvCustomerMain;
    private LocalBroadcastManager broadcastManager;
    private String search, smDate, start, status;
    private int type = RemittanceType.DEFAULT;
    private CheckBox cbTypeMain, cbStatusMain;
    private FrameLayout flClearSearchMain;
    private CodePanTextField etSearchMain;
    private boolean isInitialized, isEnd;
    private RelativeLayout rlFilterMain;
    private Handler inputFinishHandler;
    private BroadcastReceiver receiver;
    private RemittanceAdapter adapter;
    private ImageView ivFilterMain;
    private CustomerData receivedBy;
    private DrawerLayout dlMain;
    private SQLiteAdapter db;
    private ListView lvMain;
    private long lastEdit;

    @Override
    protected void onStart() {
        super.onStart();
        if (isInitialized) {
            registerReceiver();
            loadRemittance(db);
            getHandler().checkPermissions();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isInitialized) {
            unregisterReceiver();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        backupData(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        this.inputFinishHandler = new Handler(Looper.getMainLooper());
        btnShowFilterMain = findViewById(R.id.btnShowFilterMain);
        llCustomersMain = findViewById(R.id.llCustomersMain);
        llBackupMain = findViewById(R.id.llBackupMain);
        llDailySummaryMain = findViewById(R.id.llDailySummaryMain);
        llDataAnalyticsMain = findViewById(R.id.llDataAnalyticsMain);
        llRestoreBackupMain = findViewById(R.id.llRestoreBackupMain);
        flClearSearchMain = findViewById(R.id.flClearSearchMain);
        etSearchMain = findViewById(R.id.etSearchMain);
        btnStatusMain = findViewById(R.id.btnStatusMain);
        rlFilterMain = findViewById(R.id.rlFilterMain);
        btnMenuMain = findViewById(R.id.btnMenuMain);
        btnDateMain = findViewById(R.id.btnDateMain);
        btnCustomerMain = findViewById(R.id.btnCustomerMain);
        btnTypeMain = findViewById(R.id.btnTypeMain);
        btnFilterMain = findViewById(R.id.btnFilterMain);
        btnClearMain = findViewById(R.id.btnClearMain);
        ivFilterMain = findViewById(R.id.ivFilterMain);
        llMenuMain = findViewById(R.id.llMenuMain);
        tvDateMain = findViewById(R.id.tvDateMain);
        tvCustomerMain = findViewById(R.id.tvCustomerMain);
        cbStatusMain = findViewById(R.id.cbStatusMain);
        cbTypeMain = findViewById(R.id.cbTypeMain);
        dlMain = findViewById(R.id.dlMain);
        lvMain = findViewById(R.id.lvMain);
        btnShowFilterMain.setOnClickListener(this);
        btnMenuMain.setOnClickListener(this);
        btnClearMain.setOnClickListener(this);
        btnFilterMain.setOnClickListener(this);
        btnDateMain.setOnClickListener(this);
        btnCustomerMain.setOnClickListener(this);
        btnTypeMain.setOnClickListener(this);
        btnStatusMain.setOnClickListener(this);
        llCustomersMain.setOnClickListener(this);
        llDailySummaryMain.setOnClickListener(this);
        llDataAnalyticsMain.setOnClickListener(this);
        llBackupMain.setOnClickListener(this);
        llRestoreBackupMain.setOnClickListener(this);
        rlFilterMain.setOnClickListener(this);
        flClearSearchMain.setOnClickListener(this);
        int color = getResources().getColor(R.color.black_trans_twenty);
        dlMain.setScrimColor(color);
        lvMain.setOnScrollListener(this);
        lvMain.setOnItemClickListener((adapterView, view, i, l) -> {
            CodePanUtils.hideKeyboard(view, MainActivity.this);
            final RemittanceData remittance = remittanceList.get(i);
            switch(remittance.type) {
                case RemittanceType.INCOMING:
                    if(!remittance.isClaimed) {
                        if(!remittance.isMarked) {
                            ReceiveFragment receive = new ReceiveFragment();
                            receive.setRemittance(remittance);
                            receive.setOnReceiveRemittanceCallback(data -> {
                                int index = getIndex(data);
                                if(index != Result.FAILED) {
                                    remittanceList.set(index, data);
                                    updateRemittance(true);
                                }
                            });
                            transaction = manager.beginTransaction();
                            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                                R.anim.fade_in, R.anim.fade_out);
                            transaction.add(R.id.rlMain, receive);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                        else {
                            confirm(remittance);
                        }
                    }
                    else {
                        ReceiveData receive = remittance.receive;
                        if (receive != null) {
                            CustomerData customer = receive.customer;
                            if (customer != null) {
                                String receiveDate = CodePanUtils.getReadableDate(receive.dDate, true, true);
                                String time = CodePanUtils.getReadableTime(receive.dTime, false);
                                String current = CodePanUtils.getDate();
                                String date = current.equals(receive.dDate) ? "today" : "on " + receiveDate;
                                String message = "This transaction was claimed by " + customer.name + " " +
                                    date + " at " + time + ".";
                                SMPadalaLib.alertDialog(MainActivity.this, "Transaction Details", message);
                            }
                        }
                    }
                    break;
                case RemittanceType.OUTGOING:
                    TransferData transfer = remittance.transfer;
                    if (transfer != null && transfer.customer != null) {
                        String transferDate = CodePanUtils.getReadableDate(remittance.smDate, true, true);
                        String time = CodePanUtils.getReadableTime(remittance.smTime, false);
                        String current = CodePanUtils.getDate();
                        String date = current.equals(remittance.dDate) ? "today" : "on " + transferDate;
                        CustomerData customer = transfer.customer;
                        String receiver = transfer.receiver;
                        String to = receiver != null && !receiver.isEmpty() ? " to " + receiver : "";
                        String message = "This transaction was transferred by " + customer.name +
                            to + " " + date + " at " + time + ".";
                        SMPadalaLib.alertDialog(MainActivity.this, "Transaction Details", message);
                    }
                    else {
                        TransferFragment tag = new TransferFragment();
                        tag.setRemittance(remittance);
                        tag.setOnTransferRemittanceCallback(data -> {
                            int index = getIndex(data);
                            if (index != Result.FAILED) {
                                remittanceList.set(index, data);
                                updateRemittance(true);
                            }
                        });
                        transaction = manager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                            R.anim.fade_in, R.anim.fade_out);
                        transaction.add(R.id.rlMain, tag);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                    break;
            }
        });
        lvMain.setOnItemLongClickListener((parent, view, i, id) -> {
            final RemittanceData remittance = remittanceList.get(i);
            switch (remittance.type) {
                case RemittanceType.INCOMING:
                    if (remittance.isClaimed || remittance.isMarked) {
                        AlertDialogFragment alert = new AlertDialogFragment();
                        alert.setDialogTitle("Undo Transaction");
                        alert.setDialogMessage("Are you sure you want to undo this transaction?");
                        alert.setPositiveButton("Undo", view14 -> {
                            manager.popBackStack();
                            boolean result = SMPadalaLib.undoTransaction(db, remittance);
                            if (result) {
                                SMPadalaLib.alertToast(MainActivity.this,
                                    "Undo Successful");
                                remittance.isClaimed = false;
                                remittance.isMarked = false;
                                remittance.receive = null;
                                updateRemittance(true);
                            }
                        });
                        alert.setNegativeButton("Cancel", view13 -> manager.popBackStack());
                        transaction = manager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                            R.anim.fade_in, R.anim.fade_out);
                        transaction.add(R.id.rlMain, alert);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                    break;
                case RemittanceType.OUTGOING:
                    final TransferData transfer = remittance.transfer;
                    if (transfer != null && transfer.customer != null) {
                        AlertDialogFragment alert = new AlertDialogFragment();
                        alert.setDialogTitle("Untag Customer");
                        alert.setDialogMessage("Are you sure you want to untag customer?");
                        alert.setPositiveButton("Untag", view12 -> {
                            manager.popBackStack();
                            boolean result = SMPadalaLib.untagCustomer(db, transfer);
                            if (result) {
                                SMPadalaLib.alertToast(MainActivity.this,
                                    "Untag Successful");
                                remittance.transfer = null;
                                updateRemittance(true);
                            }
                        });
                        alert.setNegativeButton("Cancel", view1 -> manager.popBackStack());
                        transaction = manager.beginTransaction();
                        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                            R.anim.fade_in, R.anim.fade_out);
                        transaction.add(R.id.rlMain, alert);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                    break;
            }
            return true;
        });
        etSearchMain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search = s.toString();
                lastEdit = System.currentTimeMillis();
                inputFinishHandler.removeCallbacks(inputFinishChecker);
                inputFinishHandler.postDelayed(inputFinishChecker, IDLE_TIME);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onLoadSplash(OnInitializeCallback initializeCallback) {
        SplashFragment splash = new SplashFragment();
        splash.setOnInitializeCallback(initializeCallback);
        transaction = manager.beginTransaction();
        transaction.add(R.id.rlMain, splash);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void loadRemittance(final SQLiteAdapter db) {
        this.start = null;
        final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            updateRemittance(false);
            return true;
        });
        Thread bg = new Thread(() -> {
            try {
                remittanceList = Data.loadRemittance(db, receivedBy, search, smDate, status,
                    start, type, LIMIT);
                if (remittanceList.size() < LIMIT) {
                    isEnd = true;
                    start = null;
                }
                else {
                    isEnd = false;
                    int lastPosition = remittanceList.size() - 1;
                    start = remittanceList.get(lastPosition).ID;
                }
                handler.obtainMessage().sendToTarget();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        bg.start();
    }

    public void loadMoreRemittance(final SQLiteAdapter db) {
        final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            lvMain.setEnabled(true);
            updateRemittance(true);
            return true;
        });
        lvMain.setEnabled(false);
        Thread bg = new Thread(() -> {
            try {
                ArrayList<RemittanceData> additionalList = Data.loadRemittance(db, receivedBy, search,
                    smDate, status, start, type, LIMIT);
                remittanceList.addAll(additionalList);
                if (additionalList.size() < LIMIT) {
                    isEnd = true;
                    start = null;
                }
                else {
                    isEnd = false;
                    int lastPosition = additionalList.size() - 1;
                    start = additionalList.get(lastPosition).ID;
                }
                handler.obtainMessage().sendToTarget();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
        bg.start();
    }

    public void updateRemittance(boolean isUpdate) {
        if (isUpdate) {
            adapter.notifyDataSetChanged();
            lvMain.invalidate();
        }
        else {
            adapter = new RemittanceAdapter(MainActivity.this, remittanceList);
            lvMain.setAdapter(adapter);
        }
    }

    @Override
    public void onInitialize(SQLiteAdapter db) {
        super.onInitialize(db);
        this.isInitialized = true;
        this.db = db;
        setReceiver();
        registerReceiver();
        loadRemittance(db);
        getHandler().checkPermissions();
    }

    public void registerReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter filter = new IntentFilter(RequestCode.NOTIFICATION);
        broadcastManager.registerReceiver((receiver), filter);
    }

    public void unregisterReceiver() {
        broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.unregisterReceiver(receiver);
    }

    public void setReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(Key.NOTIFICATION)) {
                    int code = intent.getIntExtra(Key.NOTIFICATION, 0);
                    if(code == Notification.SMS_RECEIVE) {
                        loadRemittance(db);
                    }
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnMenuMain:
                if (dlMain.isDrawerOpen(llMenuMain)) {
                    dlMain.closeDrawer(llMenuMain);
                }
                else {
                    dlMain.openDrawer(llMenuMain);
                }
                break;
            case R.id.llCustomersMain:
                dlMain.closeDrawer(llMenuMain);
                final CustomerFragment customer = new CustomerFragment();
                transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out);
                transaction.add(R.id.rlMain, customer);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.llDailySummaryMain:
                dlMain.closeDrawer(llMenuMain);
                dlMain.closeDrawer(llMenuMain);
                final DailySummaryFragment summary = new DailySummaryFragment();
                transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out);
                transaction.add(R.id.rlMain, summary);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.llDataAnalyticsMain:
                dlMain.closeDrawer(llMenuMain);
                final AnalyticsFragment std = new AnalyticsFragment();
                transaction = manager.beginTransaction();
                transaction.add(R.id.rlMain, std);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.llBackupMain:
                dlMain.closeDrawer(llMenuMain);
                AlertDialogFragment backup = new AlertDialogFragment();
                backup.setDialogTitle("Back-up Data");
                backup.setDialogMessage("This will back-up your data to external storage. " +
                    "Are you sure you want to back-up data?");
                backup.setPositiveButton("Yes", v -> {
                    manager.popBackStack();
                    backupData(true);
                });
                backup.setNegativeButton("No", v -> manager.popBackStack());
                transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out);
                transaction.add(R.id.rlMain, backup);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.llRestoreBackupMain:
                dlMain.closeDrawer(llMenuMain);
                AlertDialogFragment restore = new AlertDialogFragment();
                restore.setDialogTitle(R.string.restore_backup);
                restore.setDialogMessage(text(R.string.restore_message_r, App.DB));
                restore.setPositiveButton("Browse", v -> {
                    manager.popBackStack();
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(intent, RequestCode.FILES);
                });
                restore.setNegativeButton("Cancel", v -> manager.popBackStack());
                transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out);
                transaction.add(R.id.rlMain, restore);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.btnShowFilterMain:
                if (rlFilterMain.getVisibility() == View.GONE) {
                    CodePanUtils.fadeIn(rlFilterMain);
                }
                break;
            case R.id.rlFilterMain:
                if (rlFilterMain.getVisibility() == View.VISIBLE) {
                    CodePanUtils.fadeOut(rlFilterMain);
                }
                break;
            case R.id.btnDateMain:
                CalendarDialogFragment calendar = new CalendarDialogFragment();
                calendar.setCurrentDate(smDate);
                calendar.setOnPickDateCallback(date -> {
                    String cal = CodePanUtils.getReadableDate(date, true, true);
                    tvDateMain.setText(cal);
                    smDate = date;
                });
                transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out);
                transaction.add(R.id.rlMain, calendar);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.btnCustomerMain:
                CustomerFragment search = new CustomerFragment();
                search.setOnSelectCustomerCallback(customer1 -> {
                    if (customer1 != null) {
                        tvCustomerMain.setText(customer1.name);
                        receivedBy = customer1;
                    }
                });
                transaction = manager.beginTransaction();
                transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                    R.anim.fade_in, R.anim.fade_out);
                transaction.add(R.id.rlMain, search);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.btnTypeMain:
                cbTypeMain.setChecked(!cbTypeMain.isChecked());
                break;
            case R.id.btnStatusMain:
                cbStatusMain.setChecked(!cbStatusMain.isChecked());
                break;
            case R.id.btnFilterMain:
                CodePanUtils.fadeOut(rlFilterMain);
                ivFilterMain.setVisibility(View.VISIBLE);
                this.status = !cbStatusMain.isChecked() ? RemittanceStatus.PENDING : null;
                this.type = !cbTypeMain.isChecked() ? RemittanceType.INCOMING :
                    RemittanceType.DEFAULT;
                loadRemittance(db);
                break;
            case R.id.btnClearMain:
                CodePanUtils.fadeOut(rlFilterMain);
                ivFilterMain.setVisibility(View.GONE);
                tvDateMain.setText(R.string.select_date);
                tvCustomerMain.setText(R.string.select_customer);
                cbTypeMain.setChecked(true);
                cbStatusMain.setChecked(true);
                etSearchMain.setText(null);
                type = RemittanceType.DEFAULT;
                this.receivedBy = null;
                this.smDate = null;
                this.status = null;
                this.search = null;
                loadRemittance(db);
                break;
            case R.id.flClearSearchMain:
                etSearchMain.setText(null);
                break;
        }
    }

    public void confirm(final RemittanceData remittance) {
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle("Confirm Receiving");
        alert.setDialogMessage("Are you sure you want to receive this transaction?");
        alert.setPositiveButton("Yes", view -> {
            manager.popBackStack();
            String dDate = CodePanUtils.getDate();
            String dTime = CodePanUtils.getTime();
            boolean result = SMPadalaLib.claim(db, dDate, dTime, remittance.ID);
            if (result) {
                ReceiveData receive = remittance.receive;
                receive.dDate = dDate;
                receive.dTime = dTime;
                CustomerData customer = receive.customer;
                remittance.isMarked = false;
                remittance.isClaimed = true;
                lvMain.invalidate();
                adapter.notifyDataSetChanged();
                SMPadalaLib.alertToast(MainActivity.this,
                    "Receive by " + customer.name);
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

    public void backupData(final boolean external) {
        final Handler handler = new Handler(Looper.getMainLooper(), msg -> {
            final AlertDialogFragment alert = new AlertDialogFragment();
            alert.setDialogTitle("Back-up Data Successful");
            alert.setDialogMessage("Data has been successfully backed-up. Do " +
                "you want to upload it to Google Drive?");
            alert.setPositiveButton("Yes", view -> {
                manager.popBackStack();
                if(CodePanUtils.hasInternet(MainActivity.this)) {
                    File dir = getExternalFilesDir(null);
                    if(dir != null) {
                        String path = dir.getPath() + "/" + App.DB_BACKUP + "/" + App.DB;
                        final File file = new File(path);
                        String authority = BuildConfig.APPLICATION_ID + ".provider";
                        Uri uri = FileProvider.getUriForFile(MainActivity.this, authority, file);
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent.setDataAndType(uri, "*/*");
                        intent.setPackage("com.google.android.apps.docs");
                        startActivity(intent);
                    }
                    else {
                        SMPadalaLib.alertToast(MainActivity.this, "Failed to open file");
                    }
                }
                else {
                    SMPadalaLib.alertToast(MainActivity.this, "Internet connection is required.");
                }
            });
            alert.setNegativeButton("No", view -> manager.popBackStack());
            transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_in, R.anim.fade_out);
            transaction.add(R.id.rlMain, alert);
            transaction.addToBackStack(null);
            transaction.commit();
            return true;
        });
        if (!CodePanUtils.isThreadRunning(ProcessName.BACK_UP_DB)) {
            Thread bg = new Thread(() -> {
                try {
                    boolean result = SMPadalaLib.backUpData(MainActivity.this, external);
                    if (external && result) {
                        handler.obtainMessage().sendToTarget();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            });
            bg.setName(ProcessName.BACK_UP_DB);
            bg.start();
        }
    }

    private final Runnable inputFinishChecker = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() > lastEdit + IDLE_TIME - 500) {
                loadRemittance(db);
            }
        }
    };

    public SQLiteAdapter getDatabase() {
        if (db == null) {
            db = SQLiteCache.getDatabase(this, App.DB);
        }
        return this.db;
    }

    public int getIndex(RemittanceData remittance) {
        if (remittanceList != null && remittance != null) {
            for (RemittanceData data : remittanceList) {
                if (remittance.ID != null && data.ID != null &&
                    remittance.ID.equals(data.ID)) {
                    return remittanceList.indexOf(data);
                }
            }
        }
        return Result.FAILED;
    }

    @NotNull
    @Override
    public PermissionHandler getHandler() {
        return new PermissionHandler(this, this,
            PermissionType.SMS,
            PermissionType.FILES_AND_MEDIA
        );
    }

    @Override
    public void onPermissionsResult(@NotNull PermissionHandler handler, boolean isGranted) {
        if (isGranted) {
            if (inBackStack(DialogTag.PERMISSION)) {
                manager.popBackStack();
            }
        }
        else {
            getHandler().checkPermissions();
        }
    }

    @Override
    public void onShowPermissionRationale(@NotNull PermissionHandler handler, @NotNull PermissionType permission) {
        if (notInBackStack(DialogTag.PERMISSION)) {
            String message = null;
            switch (permission) {
                case SMS:
                    message = getString(R.string.permission_sms);
                    break;
                case FILES_AND_MEDIA:
                    message = getString(R.string.permission_files_and_media);
                    break;
            }
            final AlertDialogFragment alert = new AlertDialogFragment();
            alert.setDialogTitle(R.string.permission_required);
            alert.setDialogMessage(message);
            alert.setPositiveButton(getString(R.string.settings), v -> {
                manager.popBackStack();
                handler.goToSettings();
            });
            alert.setNegativeButton(getString(R.string.cancel), v -> {
                manager.popBackStack();
                manager.popBackStack();
            });
            transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out,
                R.anim.fade_in, R.anim.fade_out);
            transaction.add(R.id.rlMain, alert, DialogTag.PERMISSION);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCode.FILES && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    File root = getExternalFilesDir(null);
                    if (root != null) {
                        File dir = new File(root.getPath() + "/" + App.DB_BACKUP);
                        if (!dir.exists()) {
                            dir.mkdir();
                        }
                        File file = new File(dir, App.DB);
                        Cursor cursor = getContentResolver().query(uri, null,
                            null, null, null);
                        if (cursor != null) {
                            boolean result = CodePanUtils.contentUriToFile(this, uri, file);
                            if (result) {
                                restoreBackup(db, file);
                            }
                            cursor.close();
                        }
                    }
                }
            }
        }
    }

    private void restoreBackup(SQLiteAdapter db, File file) {
         boolean result = false;
        File dir = getExternalFilesDir(null);
        if (dir != null) {
            File destination = getDatabasePath(App.DB);
            try {
                db.close();
                CodePanUtils.copyFile(file, destination);
                this.db = SQLiteCache.getDatabase(this, App.DB);
                this.db.openConnection();
                loadRemittance(db);
                result = true;
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        showRestorationResult(result);
    }

    private void showRestorationResult(final boolean result) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(result) {
                SMPadalaLib.alertToast(this, "Back-up has been successfully restored.");
            }
            else {
                SMPadalaLib.alertToast(this, "Failed to restore back-up file.");
            }
        }, 500L);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch(scrollState) {
            case SCROLL_STATE_TOUCH_SCROLL:
                etSearchMain.clearFocus();
                CodePanUtils.hideKeyboard(etSearchMain, MainActivity.this);
                break;
            case SCROLL_STATE_IDLE:
                if(firstVisible == totalItem - visibleItem & !isEnd) {
                    loadMoreRemittance(db);
                }
                break;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisible,
        int visibleItem, int totalItem) {
        this.firstVisible = firstVisible;
        this.visibleItem = visibleItem;
        this.totalItem = totalItem;
    }
}
