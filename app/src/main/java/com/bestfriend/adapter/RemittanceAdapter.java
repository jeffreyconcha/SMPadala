package com.bestfriend.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bestfriend.constant.RemittanceType;
import com.bestfriend.model.CustomerData;
import com.bestfriend.model.ReceiveData;
import com.bestfriend.model.RemittanceData;
import com.bestfriend.model.TransferData;
import com.bestfriend.smpadala.R;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanLabel;

import java.text.NumberFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RemittanceAdapter extends ArrayAdapter<RemittanceData> {

    private final ArrayList<RemittanceData> items;
    private final LayoutInflater inflater;
    private final NumberFormat nf;
    private final int red;
    private final int gray;
    private final int theme;

    public RemittanceAdapter(@NonNull  Context context, ArrayList<RemittanceData> items) {
        super(context, 0, items);
        this.inflater = LayoutInflater.from(context);
        this.items = items;
        this.nf = NumberFormat.getInstance();
        this.nf.setMaximumFractionDigits(2);
        this.nf.setMinimumFractionDigits(2);
        this.nf.setGroupingUsed(true);
        Resources res = context.getResources();
        this.red = res.getColor(R.color.red_pri);
        this.gray = res.getColor(R.color.gray_pri);
        this.theme = res.getColor(R.color.theme_ter);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        final RemittanceData data = items.get(position);
        if (data != null) {
            if (view == null) {
                view = inflater.inflate(R.layout.remittance_list_row, parent, false);
                holder = new ViewHolder();
                holder.tvDateRemittance = view.findViewById(R.id.tvDateRemittance);
                holder.tvTimeRemittance = view.findViewById(R.id.tvTimeRemittance);
                holder.tvNameRemittance = view.findViewById(R.id.tvNameRemittance);
                holder.tvChargeRemittance = view.findViewById(R.id.tvChargeRemittance);
                holder.tvAmountRemittance = view.findViewById(R.id.tvAmountRemittance);
                holder.tvBalanceRemittance = view.findViewById(R.id.tvBalanceRemittance);
                holder.tvReferenceNoRemittance = view.findViewById(R.id.tvReferenceNoRemittance);
                holder.tvStatusRemittance = view.findViewById(R.id.tvStatusRemittance);
                view.setTag(holder);
            }
            else {
                holder = (ViewHolder) view.getTag();
            }
            if (data.smDate != null) {
                String date = CodePanUtils.getReadableDate(data.smDate, true, true);
                holder.tvDateRemittance.setText(date);
            }
            holder.tvTimeRemittance.setText(data.smTime);
            CustomerData customer = null;
            switch (data.type) {
                case RemittanceType.INCOMING:
                    ReceiveData receive = data.receive;
                    if (receive != null) {
                        customer = receive.customer;
                    }
                    break;
                case RemittanceType.OUTGOING:
                    TransferData transfer = data.transfer;
                    if (transfer != null) {
                        customer = transfer.customer;
                    }
                    break;
            }
            if(customer != null) {
                holder.tvNameRemittance.setText(customer.name);
            }
            else {
                holder.tvNameRemittance.setText(null);
            }
            holder.tvChargeRemittance.setText(nf.format(data.charge));
            if(data.amount != 0) {
                holder.tvAmountRemittance.setText(nf.format(data.amount));
            }
            else {
                holder.tvAmountRemittance.setText(R.string.na);
            }
            if(data.hasBalance) {
                holder.tvBalanceRemittance.setText(nf.format(data.balance));
            }
            else {
                holder.tvBalanceRemittance.setText(R.string.na);
            }
            holder.tvReferenceNoRemittance.setText(data.referenceNo);
            switch(data.type) {
                case RemittanceType.INCOMING:
                    int status = data.isClaimed ? R.string.claimed : R.string.pending;
                    holder.tvStatusRemittance.setText(status);
                    if(!data.isMarked) {
                        holder.tvDateRemittance.setTextColor(gray);
                        holder.tvTimeRemittance.setTextColor(gray);
                        holder.tvNameRemittance.setTextColor(gray);
                        holder.tvChargeRemittance.setTextColor(gray);
                        holder.tvAmountRemittance.setTextColor(gray);
                        holder.tvBalanceRemittance.setTextColor(gray);
                        holder.tvReferenceNoRemittance.setTextColor(gray);
                        holder.tvStatusRemittance.setTextColor(gray);
                    }
                    else {
                        holder.tvDateRemittance.setTextColor(theme);
                        holder.tvTimeRemittance.setTextColor(theme);
                        holder.tvNameRemittance.setTextColor(theme);
                        holder.tvChargeRemittance.setTextColor(theme);
                        holder.tvAmountRemittance.setTextColor(theme);
                        holder.tvBalanceRemittance.setTextColor(theme);
                        holder.tvReferenceNoRemittance.setTextColor(theme);
                        holder.tvStatusRemittance.setTextColor(theme);
                    }
                    break;
                case RemittanceType.OUTGOING:
                    holder.tvStatusRemittance.setText(R.string.na);
                    holder.tvDateRemittance.setTextColor(red);
                    holder.tvTimeRemittance.setTextColor(red);
                    holder.tvNameRemittance.setTextColor(red);
                    holder.tvChargeRemittance.setTextColor(red);
                    holder.tvAmountRemittance.setTextColor(red);
                    holder.tvBalanceRemittance.setTextColor(red);
                    holder.tvReferenceNoRemittance.setTextColor(red);
                    holder.tvStatusRemittance.setTextColor(red);
                    break;
            }
        }
        return view;
    }

    private static class ViewHolder {
        private CodePanLabel tvDateRemittance;
        private CodePanLabel tvTimeRemittance;
        private CodePanLabel tvNameRemittance;
        private CodePanLabel tvChargeRemittance;
        private CodePanLabel tvAmountRemittance;
        private CodePanLabel tvBalanceRemittance;
        private CodePanLabel tvReferenceNoRemittance;
        private CodePanLabel tvStatusRemittance;
    }
}
