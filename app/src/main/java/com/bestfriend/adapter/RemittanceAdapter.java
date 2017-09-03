package com.bestfriend.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bestfriend.constant.RemittanceType;
import com.bestfriend.model.CustomerObj;
import com.bestfriend.model.ReceiveObj;
import com.bestfriend.model.RemittanceObj;
import com.bestfriend.smpadala.R;
import com.codepan.utils.CodePanUtils;
import com.codepan.widget.CodePanLabel;

import java.text.NumberFormat;
import java.util.ArrayList;

public class RemittanceAdapter extends ArrayAdapter<RemittanceObj> {

    private ArrayList<RemittanceObj> items;
    private LayoutInflater inflater;
    private NumberFormat nf;
    private int red, gray;

    public RemittanceAdapter(Context context, ArrayList<RemittanceObj> items) {
        super(context, 0, items);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = items;
        this.nf = NumberFormat.getInstance();
        this.nf.setMaximumFractionDigits(2);
        this.nf.setMinimumFractionDigits(2);
        this.nf.setGroupingUsed(true);
        Resources res = context.getResources();
        this.red = res.getColor(R.color.red_pri);
        this.gray = res.getColor(R.color.gray_pri);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        ViewHolder holder;
        final RemittanceObj obj = items.get(position);
        if(obj != null) {
            if(view == null) {
                view = inflater.inflate(R.layout.remittance_list_row, parent, false);
                holder = new ViewHolder();
                holder.tvDateRemittance = (CodePanLabel) view.findViewById(R.id.tvDateRemittance);
                holder.tvTimeRemittance = (CodePanLabel) view.findViewById(R.id.tvTimeRemittance);
                holder.tvNameRemittance = (CodePanLabel) view.findViewById(R.id.tvNameRemittance);
                holder.tvChargeRemittance = (CodePanLabel) view.findViewById(R.id.tvChargeRemittance);
                holder.tvAmountRemittance = (CodePanLabel) view.findViewById(R.id.tvAmountRemittance);
                holder.tvBalanceRemittance = (CodePanLabel) view.findViewById(R.id.tvBalanceRemittance);
                holder.tvReferenceNoRemittance = (CodePanLabel) view.findViewById(R.id.tvReferenceNoRemittance);
                holder.tvStatusRemittance = (CodePanLabel) view.findViewById(R.id.tvStatusRemittance);
                view.setTag(holder);
            }
            else {
                holder = (ViewHolder) view.getTag();
            }
            if(obj.smDate != null) {
                String date = CodePanUtils.getCalendarDate(obj.smDate, true, true);
                holder.tvDateRemittance.setText(date);
            }
            holder.tvTimeRemittance.setText(obj.smTime);
            ReceiveObj receive = obj.receive;
            if(receive != null) {
                CustomerObj customer = receive.customer;
                if(customer != null) {
                    holder.tvNameRemittance.setText(customer.name);
                }
            }
            else {
                holder.tvNameRemittance.setText(null);
            }
            holder.tvChargeRemittance.setText(nf.format(obj.charge));
            holder.tvAmountRemittance.setText(nf.format(obj.amount));
            if(obj.hasBalance) {
                holder.tvBalanceRemittance.setText(nf.format(obj.balance));
            }
            else {
                holder.tvBalanceRemittance.setText(R.string.pending);
            }
            holder.tvReferenceNoRemittance.setText(obj.referenceNo);
            switch(obj.type) {
                case RemittanceType.RECEIVE:
                    int status = obj.isClaimed ? R.string.claimed : R.string.pending;
                    holder.tvStatusRemittance.setText(status);
                    holder.tvDateRemittance.setTextColor(gray);
                    holder.tvTimeRemittance.setTextColor(gray);
                    holder.tvNameRemittance.setTextColor(gray);
                    holder.tvChargeRemittance.setTextColor(gray);
                    holder.tvAmountRemittance.setTextColor(gray);
                    holder.tvBalanceRemittance.setTextColor(gray);
                    holder.tvReferenceNoRemittance.setTextColor(gray);
                    holder.tvStatusRemittance.setTextColor(gray);
                    break;
                case RemittanceType.TRANSFER:
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

    private class ViewHolder {
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
