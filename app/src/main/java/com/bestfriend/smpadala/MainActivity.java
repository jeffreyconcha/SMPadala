package com.bestfriend.smpadala;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.bestfriend.adapter.RemittanceAdapter;
import com.bestfriend.cache.SQLiteCache;
import com.bestfriend.callback.Interface.OnInitializeCallback;
import com.bestfriend.callback.Interface.OnReceiveRemittanceCallback;
import com.bestfriend.callback.Interface.OnSelectCustomerCallback;
import com.bestfriend.callback.Interface.OnTransferRemittanceCallback;
import com.bestfriend.constant.App;
import com.bestfriend.constant.Key;
import com.bestfriend.constant.Notification;
import com.bestfriend.constant.ProcessName;
import com.bestfriend.constant.RemittanceStatus;
import com.bestfriend.constant.RemittanceType;
import com.bestfriend.constant.RequestCode;
import com.bestfriend.core.Data;
import com.bestfriend.core.SMPadalaLib;
import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.ReceiveObj;
import com.bestfriend.model.RemittanceObj;
import com.bestfriend.model.TransferObj;
import com.codepan.calendar.callback.Interface.OnPickDateCallback;
import com.codepan.calendar.view.CalendarView;
import com.codepan.callback.Interface.OnPermissionGrantedCallback;
import com.codepan.database.SQLiteAdapter;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanButton;
import com.codepan.widget.CodePanLabel;
import com.codepan.widget.CodePanTextField;

import java.util.ArrayList;

import static android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE;

public class MainActivity extends FragmentActivity implements OnInitializeCallback, OnClickListener {

    private final int LIMIT = 200;
    private final long IDLE_TIME = 500;

    private CodePanButton btnMenuMain, btnShowFilterMain, btnDateMain, btnTypeMain,
            btnStatusMain, btnCustomerMain, btnFilterMain, btnClearMain;
    private LinearLayout llMenuMain, llCustomersMain, llDailyReportMain, llBackUpMain;
    private OnPermissionGrantedCallback permissionGrantedCallback;
    private int visibleItem, totalItem, firstVisible;
    private ArrayList<RemittanceObj> remittanceList;
    private CodePanLabel tvDateMain, tvCustomerMain;
    private LocalBroadcastManager broadcastManager;
    private String search, smDate, start, status;
    private int type = RemittanceType.DEFAULT;
    private CheckBox cbTypeMain, cbStatusMain;
    private FragmentTransaction transaction;
    private CodePanTextField etSearchMain;
    private boolean isInitialized, isEnd;
    private RelativeLayout rlFilterMain;
    private Handler inputFinishHandler;
    private BroadcastReceiver receiver;
    private RemittanceAdapter adapter;
    private FragmentManager manager;
    private ImageView ivFilterMain;
    private CustomerObj receivedBy;
    private DrawerLayout dlMain;
    private SQLiteAdapter db;
    private ListView lvMain;
    private long lastEdit;

    @Override
    protected void onStart() {
        super.onStart();
        if(isInitialized) {
            registerReceiver();
            loadRemittance(db);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isInitialized) {
            unregisterReceiver();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        backUpData(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        this.manager = getSupportFragmentManager();
        this.inputFinishHandler = new Handler();
        btnShowFilterMain = findViewById(R.id.btnShowFilterMain);
        llCustomersMain = findViewById(R.id.llCustomersMain);
        llBackUpMain = findViewById(R.id.llBackUpMain);
        llDailyReportMain = findViewById(R.id.llDailyReportMain);
        etSearchMain = findViewById(R.id.etSearchMain);
        btnStatusMain = findViewById(R.id.btnStatusMain);
        rlFilterMain = findViewById(R.id.rlFilterMain);
        llBackUpMain = findViewById(R.id.llBackUpMain);
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
        llBackUpMain.setOnClickListener(this);
        llDailyReportMain.setOnClickListener(this);
        llCustomersMain.setOnClickListener(this);
        rlFilterMain.setOnClickListener(this);
        int color = getResources().getColor(R.color.black_trans_twenty);
        dlMain.setScrimColor(color);
        lvMain.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                CodePanUtils.hideKeyboard(view, MainActivity.this);
                final RemittanceObj remittance = remittanceList.get(i);
                switch(remittance.type) {
                    case RemittanceType.RECEIVE:
                        if(!remittance.isClaimed) {
                            if(!remittance.isMarked) {
                                ReceiveFragment receive = new ReceiveFragment();
                                receive.setRemittance(remittance);
                                receive.setOnReceiveRemittanceCallback(new OnReceiveRemittanceCallback() {
                                    @Override
                                    public void onReceiveRemittance(RemittanceObj obj) {
                                        remittanceList.set(i, obj);
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
                            ReceiveObj receive = remittance.receive;
                            if(receive != null) {
                                CustomerObj customer = receive.customer;
                                if(customer != null) {
                                    String receiveDate = CodePanUtils.getCalendarDate(receive.dDate, true, true);
                                    String time = CodePanUtils.getNormalTime(receive.dTime, false);
                                    String current = CodePanUtils.getDate();
                                    String date = current.equals(receive.dDate) ? "today" : "on " + receiveDate;
                                    String message = "This transaction was claimed by " + customer.name + " " +
                                            date + " at " + time + ".";
                                    SMPadalaLib.alertDialog(MainActivity.this, "Transaction Details", message);
                                }
                            }
                        }
                        break;
                    case RemittanceType.TRANSFER:
                        TransferObj transfer = remittance.transfer;
                        if(transfer != null && transfer.customer != null) {
                            String transferDate = CodePanUtils.getCalendarDate(remittance.smDate, true, true);
                            String time = CodePanUtils.getNormalTime(remittance.smTime, false);
                            String current = CodePanUtils.getDate();
                            String date = current.equals(remittance.dDate) ? "today" : "on " + transferDate;
                            CustomerObj customer = transfer.customer;
                            String receiver = transfer.receiver;
                            String to = receiver != null && !receiver.isEmpty() ? " to " + receiver : "";
                            String message = "This transaction was transferred by " + customer.name +
                                    to + " " + date + " at " + time + ".";
                            SMPadalaLib.alertDialog(MainActivity.this, "Transaction Details", message);
                        }
                        else {
                            TransferFragment tag = new TransferFragment();
                            tag.setRemittance(remittance);
                            tag.setOnTransferRemittanceCallback(new OnTransferRemittanceCallback() {
                                @Override
                                public void onTransferRemittance(RemittanceObj obj) {
                                    remittanceList.set(i, obj);
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
            }
        });
        lvMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int i, long id) {
                final RemittanceObj remittance = remittanceList.get(i);
                switch(remittance.type) {
                    case RemittanceType.RECEIVE:
                        if(remittance.isClaimed || remittance.isMarked) {
                            AlertDialogFragment alert = new AlertDialogFragment();
                            alert.setDialogTitle("Undo Transaction");
                            alert.setDialogMessage("Are you sure you want to undo this transaction?");
                            alert.setPositiveButton("Undo", new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    manager.popBackStack();
                                    boolean result = SMPadalaLib.undoTransaction(db, remittance);
                                    if(result) {
                                        CodePanUtils.alertToast(MainActivity.this,
                                                "Undo Successful");
                                        remittance.isClaimed = false;
                                        remittance.isMarked = false;
                                        remittance.receive = null;
                                        updateRemittance(true);
                                    }
                                }
                            });
                            alert.setNegativeButton("Cancel", new OnClickListener() {
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
                        break;
                    case RemittanceType.TRANSFER:
                        final TransferObj transfer = remittance.transfer;
                        if(transfer != null && transfer.customer != null) {
                            AlertDialogFragment alert = new AlertDialogFragment();
                            alert.setDialogTitle("Untag Customer");
                            alert.setDialogMessage("Are you sure you want to untag customer?");
                            alert.setPositiveButton("Untag", new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    manager.popBackStack();
                                    boolean result = SMPadalaLib.untagCustomer(db, transfer);
                                    if(result) {
                                        CodePanUtils.alertToast(MainActivity.this,
                                                "Untag Successful");
                                        remittance.transfer = null;
                                        updateRemittance(true);
                                    }
                                }
                            });
                            alert.setNegativeButton("Cancel", new OnClickListener() {
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
                        break;
                }
                return true;
            }
        });
        lvMain.setOnScrollListener(new AbsListView.OnScrollListener() {
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
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
                firstVisible = firstVisibleItem;
                visibleItem = visibleItemCount;
                totalItem = totalItemCount;
            }
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
        init(savedInstanceState);
    }

    public void loadRemittance(final SQLiteAdapter db) {
        this.start = null;
        final Handler handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                updateRemittance(false);
                return true;
            }
        });
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    remittanceList = Data.loadRemittance(db, receivedBy, search, smDate, status,
                            start, type, LIMIT);
                    if(remittanceList.size() < LIMIT) {
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
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bg.start();
    }

    public void loadMoreRemittance(final SQLiteAdapter db) {
        final Handler handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                lvMain.setEnabled(true);
                updateRemittance(true);
                return true;
            }
        });
        lvMain.setEnabled(false);
        Thread bg = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<RemittanceObj> additionalList = Data.loadRemittance(db, receivedBy, search,
                            smDate, status, start, type, LIMIT);
                    remittanceList.addAll(additionalList);
                    if(additionalList.size() < LIMIT) {
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
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        bg.start();
    }

    public void updateRemittance(boolean isUpdate) {
        if(isUpdate) {
            adapter.notifyDataSetChanged();
            lvMain.invalidate();
        }
        else {
            adapter = new RemittanceAdapter(MainActivity.this, remittanceList);
            lvMain.setAdapter(adapter);
        }
    }

    public void init(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            if(isInitialized) {
                checkRevokedPermissions();
            }
            else {
                this.finish();
                overridePendingTransition(0, 0);
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }
        else {
            SplashFragment splash = new SplashFragment();
            splash.setOnInitializeCallback(this);
            transaction = manager.beginTransaction();
            transaction.add(R.id.rlMain, splash);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    public void checkRevokedPermissions() {
        if(!CodePanUtils.isPermissionGranted(this)) {
            manager.popBackStack(null, POP_BACK_STACK_INCLUSIVE);
            Intent intent = new Intent(this, getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            this.finish();
        }
    }

    public void setOnPermissionGrantedCallback(OnPermissionGrantedCallback permissionGrantedCallback) {
        this.permissionGrantedCallback = permissionGrantedCallback;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        switch(requestCode) {
            case RequestCode.PERMISSION:
                if(grantResults.length > 0) {
                    boolean isPermissionGranted = true;
                    for(int result : grantResults) {
                        if(result == PackageManager.PERMISSION_DENIED) {
                            isPermissionGranted = false;
                            break;
                        }
                    }
                    if(permissionGrantedCallback != null) {
                        permissionGrantedCallback.onPermissionGranted(isPermissionGranted);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onInitialize(SQLiteAdapter db) {
        this.isInitialized = true;
        this.db = db;
        setReceiver();
        registerReceiver();
        loadRemittance(db);
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
                if(intent.hasExtra(Key.NOTIFICATION)) {
                    int code = intent.getIntExtra(Key.NOTIFICATION, 0);
                    switch(code) {
                        case Notification.SMS_RECEIVE:
                            loadRemittance(db);
                            break;
                    }
                }
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnMenuMain:
                if(dlMain.isDrawerOpen(llMenuMain)) {
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
            case R.id.llDailyReportMain:
                dlMain.closeDrawer(llMenuMain);
                SalesToDateFragment std = new SalesToDateFragment();
                transaction = manager.beginTransaction();
                transaction.add(R.id.rlMain, std);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            case R.id.llBackUpMain:
                dlMain.closeDrawer(llMenuMain);
                AlertDialogFragment alert = new AlertDialogFragment();
                alert.setDialogTitle("Back-up Data");
                alert.setDialogMessage("This will back-up your data to external storage. " +
                        "Are you sure you want to back-up data?");
                alert.setPositiveButton("Yes", new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        manager.popBackStack();
                        backUpData(true);
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
                break;
            case R.id.btnShowFilterMain:
                if(rlFilterMain.getVisibility() == View.GONE) {
                    CodePanUtils.fadeIn(rlFilterMain);
                }
                break;
            case R.id.rlFilterMain:
                if(rlFilterMain.getVisibility() == View.VISIBLE) {
                    CodePanUtils.fadeOut(rlFilterMain);
                }
                break;
            case R.id.btnDateMain:
                CalendarView calendar = new CalendarView();
                calendar.setCurrentDate(smDate);
                calendar.setOnPickDateCallback(new OnPickDateCallback() {
                    @Override
                    public void onPickDate(String date) {
                        String cal = CodePanUtils.getCalendarDate(date, true, true);
                        tvDateMain.setText(cal);
                        smDate = date;
                    }
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
                search.setOnSelectCustomerCallback(new OnSelectCustomerCallback() {
                    @Override
                    public void onSelectCustomer(CustomerObj customer) {
                        if(customer != null) {
                            tvCustomerMain.setText(customer.name);
                            receivedBy = customer;
                        }
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
                this.type = !cbTypeMain.isChecked() ? RemittanceType.RECEIVE :
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
        }
    }

    public void confirm(final RemittanceObj remittance) {
        final AlertDialogFragment alert = new AlertDialogFragment();
        alert.setDialogTitle("Confirm Receiving");
        alert.setDialogMessage("Are you sure you want to receive this transaction?");
        alert.setPositiveButton("Yes", new OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.popBackStack();
                String dDate = CodePanUtils.getDate();
                String dTime = CodePanUtils.getTime();
                boolean result = SMPadalaLib.claim(db, dDate, dTime, remittance.ID);
                if(result) {
                    ReceiveObj receive = remittance.receive;
                    receive.dDate = dDate;
                    receive.dTime = dTime;
                    CustomerObj customer = receive.customer;
                    remittance.isMarked = false;
                    remittance.isClaimed = true;
                    lvMain.invalidate();
                    adapter.notifyDataSetChanged();
                    CodePanUtils.alertToast(MainActivity.this,
                            "Receive by " + customer.name);
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

    public void backUpData(final boolean external) {
        final Handler handler = new Handler(new Callback() {
            @Override
            public boolean handleMessage(Message message) {
                CodePanUtils.alertToast(MainActivity.this, "Data has " +
                        "been successfully backed-up.");
                return true;
            }
        });
        if(!CodePanUtils.isThreadRunning(ProcessName.BACK_UP_DB)) {
            Thread bg = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean result = SMPadalaLib.backUpData(MainActivity.this, external);
                        if(external && result) {
                            handler.obtainMessage().sendToTarget();
                        }
                    }
                    catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            bg.setName(ProcessName.BACK_UP_DB);
            bg.start();
        }
    }

    private Runnable inputFinishChecker = new Runnable() {
        @Override
        public void run() {
            if(System.currentTimeMillis() > lastEdit + IDLE_TIME - 500) {
                loadRemittance(db);
            }
        }
    };

    public SQLiteAdapter getDatabase() {
        if(db == null) {
            db = SQLiteCache.getDatabase(this, App.DB);
        }
        return this.db;
    }
}
