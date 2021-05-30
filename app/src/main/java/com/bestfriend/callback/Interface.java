package com.bestfriend.callback;

import com.bestfriend.model.CustomerData;
import com.bestfriend.model.RemittanceData;

public class Interface {

	public interface OnReceiveRemittanceCallback {
		void onReceiveRemittance(RemittanceData remittance);
	}

	public interface OnTransferRemittanceCallback {
		void onTransferRemittance(RemittanceData remittance);
	}

	public interface OnRetakeCameraCallback {
		void onRetakeCamera();
	}

	public interface OnUsePhotoCallback {
		void onUsePhoto(String fileName);
	}

	public interface OnSaveCustomerCallback {
		void onSaveCustomer(CustomerData customer);
	}

	public interface OnSelectCustomerCallback {
		void onSelectCustomer(CustomerData customer);
	}
}
