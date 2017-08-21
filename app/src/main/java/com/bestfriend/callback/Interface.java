package com.bestfriend.callback;

import com.codepan.database.SQLiteAdapter;

public class Interface {

    public interface OnInitializeCallback{
        void onInitialize(SQLiteAdapter db);
    }
}
