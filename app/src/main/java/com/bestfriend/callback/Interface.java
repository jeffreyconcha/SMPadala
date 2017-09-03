package com.bestfriend.callback;

import com.bestfriend.model.RemittanceObj;
import com.codepan.database.SQLiteAdapter;

public class Interface {

    public interface OnInitializeCallback {
        void onInitialize(SQLiteAdapter db);
    }

    public interface OnReceiveRemittanceCallback {
        void onReceiveRemittance(RemittanceObj remittance);
    }

    public interface OnRetakeCameraCallback {
        void onRetakeCamera();
    }

    public interface OnUsePhotoCallback {
        void onUsePhoto(String fileName);
    }
}
