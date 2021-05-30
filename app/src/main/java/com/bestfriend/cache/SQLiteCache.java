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
                SQLiteAdapter db = new SQLiteAdapter(context, name, key(), App.DB_PWD, App.DB_VERSION);
                db.setOnCreateDatabaseCallback(SMPadalaLib::createTables);
                db.setOnUpgradeDatabaseCallback((db1, ov, nv) -> SMPadalaLib.createTables(db1));
                CACHE.put(name, db);
            }
        }
        return CACHE.get(name);
    }
}
