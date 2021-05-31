package com.bestfriend.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bestfriend.model.DailySummaryData;
import com.bestfriend.smpadala.R;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanLabel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
public class DailySummaryAdapter extends ArrayAdapter<DailySummaryData> {

	private final ArrayList<DailySummaryData> items;
	private final LayoutInflater inflater;
	private final DecimalFormat df;
	private final NumberFormat nf;

	public DailySummaryAdapter(@NonNull Context context, ArrayList<DailySummaryData> items) {
		super(context, 0, items);
		this.inflater = LayoutInflater.from(context);
		this.items = items;
		this.nf = NumberFormat.getInstance();
		this.nf.setMaximumFractionDigits(2);
		this.nf.setMinimumFractionDigits(2);
		this.nf.setGroupingUsed(true);
		this.df = new DecimalFormat("#");
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		final DailySummaryData data = items.get(position);
		if(data != null) {
			if(view == null) {
				view = inflater.inflate(R.layout.daily_summary_list_row, parent, false);
				holder = new ViewHolder();
				holder.tvDateDailySummary = view.findViewById(R.id.tvDateDailySummary);
				holder.tvTotalAmountDailySummary = view.findViewById(R.id.tvTotalAmountDailySummary);
				holder.tvTotalChargeDailySummary = view.findViewById(R.id.tvTotalChargeDailySummary);
				holder.tvTotalCountDailySummary = view.findViewById(R.id.tvTotalCountDailySummary);
				holder.tvTransferAmountDailySummary = view.findViewById(R.id.tvTransferAmountDailySummary);
				holder.tvTransferChargeDailySummary = view.findViewById(R.id.tvTransferChargeDailySummary);
				holder.tvTransferCountDailySummary = view.findViewById(R.id.tvTransferCountDailySummary);
				holder.tvReceiveAmountDailySummary = view.findViewById(R.id.tvReceiveAmountDailySummary);
				holder.tvReceiveChargeDailySummary = view.findViewById(R.id.tvReceiveChargeDailySummary);
				holder.tvReceiveCountDailySummary = view.findViewById(R.id.tvReceiveCountDailySummary);
				holder.tvReceiveUnclaimedDailySummary = view.findViewById(R.id.tvReceiveUnclaimedDailySummary);
				view.setTag(holder);
			}
			else {
				holder = (ViewHolder) view.getTag();
			}
			String date = CodePanUtils.getReadableDate(data.date, true, true);
			holder.tvDateDailySummary.setText(date);
			holder.tvTotalAmountDailySummary.setText(nf.format(data.totalAmount));
			holder.tvTotalChargeDailySummary.setText(nf.format(data.totalCharge));
			holder.tvTotalCountDailySummary.setText(String.valueOf(data.totalCount));
			holder.tvTransferAmountDailySummary.setText(nf.format(data.transferAmount));
			holder.tvTransferChargeDailySummary.setText(nf.format(data.transferCharge));
			holder.tvTransferCountDailySummary.setText(String.valueOf(data.transferCount));
			holder.tvReceiveAmountDailySummary.setText(nf.format(data.receiveAmount));
			holder.tvReceiveChargeDailySummary.setText(nf.format(data.receiveCharge));
			holder.tvReceiveCountDailySummary.setText(String.valueOf(data.receiveCount));
			int unclaimed = data.receiveCount - data.receiveClaimed;
			holder.tvReceiveUnclaimedDailySummary.setText(String.valueOf(unclaimed));
		}
		return view;
	}

	private static class ViewHolder {
		private CodePanLabel tvDateDailySummary;
		private CodePanLabel tvTotalAmountDailySummary;
		private CodePanLabel tvTotalChargeDailySummary;
		private CodePanLabel tvTotalCountDailySummary;
		private CodePanLabel tvTransferAmountDailySummary;
		private CodePanLabel tvTransferChargeDailySummary;
		private CodePanLabel tvTransferCountDailySummary;
		private CodePanLabel tvReceiveAmountDailySummary;
		private CodePanLabel tvReceiveChargeDailySummary;
		private CodePanLabel tvReceiveCountDailySummary;
		private CodePanLabel tvReceiveUnclaimedDailySummary;
	}
}
