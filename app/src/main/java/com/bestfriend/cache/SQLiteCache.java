package com.bestfriend.cache;

import android.content.Context;
import android.util.Log;

import com.bestfriend.core.SMPadalaLib;
import com.codepan.callback.Interface.OnCreateDatabaseCallback;
import com.codepan.callback.Interface.OnUpgradeDatabaseCallback;
import com.bestfriend.constant.App;
import com.codepan.database.SQLiteAdapter;

import java.util.Hashtable;

public class SQLiteCache {

    private static final Hashtable<String, SQLiteAdapter> CACHE = new Hashtable<>();
    private static native String key();

    static {
        System.loadLibrary("ndkLib");
    }

    public static SQLiteAdapter getDatabase(Context context, String name) {
        synchronized(CACHE) {
            if(!CACHE.containsKey(name)) {
                SQLiteAdapter db = new SQLiteAdapter(context, name, key(), App.DB_PWD, App.DB_VERSION);
                db.setOnCreateDatabaseCallback(new OnCreateDatabaseCallback() {
                    @Override
                    public void onCreateDatabase(SQLiteAdapter db) {
                        SMPadalaLib.createTables(db);
                    }
                });
                db.setOnUpgradeDatabaseCallback(new OnUpgradeDatabaseCallback() {
                    @Override
                    public void onUpgradeDatabase(SQLiteAdapter db, int ov, int nv) {
                        SMPadalaLib.createTables(db);
                        SMPadalaLib.updateTables(db, ov, nv);
                    }
                });
                CACHE.put(name, db);
            }
        }
        return CACHE.get(name);
    }
}
