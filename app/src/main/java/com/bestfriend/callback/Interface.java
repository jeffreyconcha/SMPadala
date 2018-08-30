package com.bestfriend.callback;

import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.RemittanceObj;
import com.codepan.database.SQLiteAdapter;

public class Interface {

	public interface OnInitializeCallback {
		void onInitialize(SQLiteAdapter db);
	}

	public interface OnReceiveRemittanceCallback {
		void onReceiveRemittance(RemittanceObj remittance);
	}

	public interface OnTransferRemittanceCallback {
		void onTransferRemittance(RemittanceObj remittance);
	}

	public interface OnRetakeCameraCallback {
		void onRetakeCamera();
	}

	public interface OnUsePhotoCallback {
		void onUsePhoto(String fileName);
	}

	public interface OnSaveCustomerCallback {
		void onSaveCustomer(CustomerObj customer);
	}

	public interface OnSelectCustomerCallback {
		void onSelectCustomer(CustomerObj customer);
	}
}
