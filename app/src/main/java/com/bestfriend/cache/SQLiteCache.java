package com.bestfriend.cache;

import android.content.Context;

import com.bestfriend.constant.App;
import com.bestfriend.core.SMPadalaLib;
import com.codepan.database.SQLiteAdapter;

import java.util.Hashtable;

public class SQLiteCache {

    private static final Hashtable<String, SQLiteAdapter> CACHE = new Hashtable<>();

    private static native String key();

    static {
        System.loadLibrary("smpadala");
    }

    public static SQLiteAdapter getDatabase(Context context, String name) {
        synchronized(CACHE) {
            if(!CACHE.containsKey(name)) {
                SQLiteAdapter database = new SQLiteAdapter(context, name, key(), App.DB_PWD, App.DB_VERSION);
                database.setOnCreateDatabaseCallback(db -> {
                    SMPadalaLib.createTables(db);
                    SMPadalaLib.createIndexes(db);
                });
                database.setOnUpgradeDatabaseCallback((db, ov, nv) -> {
                    SMPadalaLib.createTables(db);
                    SMPadalaLib.updateTables(db, ov, nv);
                    SMPadalaLib.createIndexes(db);
                    SMPadalaLib.fixData(db);
                });
                CACHE.put(name, database);
            }
        }
        return CACHE.get(name);
    }
}
