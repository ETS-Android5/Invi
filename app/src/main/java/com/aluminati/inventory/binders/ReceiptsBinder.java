package com.aluminati.inventory.binders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.receipt.ReceiptItem;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReceiptsBinder implements IBinder<ReceiptItem> {
    private TextView tvRecTitle, tvRecTotal, tvRecQty, tvRecDesc;
    private View itemView;
    private Context context;
    private SimpleDateFormat simpleDateFormat;

    public ReceiptsBinder() {
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public View initViews(View view) {
        this.itemView = view;

        tvRecTitle = itemView.findViewById(R.id.tvRecTitle);
        tvRecTotal = itemView.findViewById(R.id.tvRecTotal);
        tvRecQty = itemView.findViewById(R.id.tvRecQty);
        tvRecDesc = itemView.findViewById(R.id.tvRecDesc);

        return itemView;
    }


    public ReceiptItem bind(ReceiptItem item, Context context) {
        this.context = context;

        tvRecTitle.setText(item.isRental() ? "Rental Receipt" : "Purchase Receipt");

        long time = Long.parseLong(item.getTimestamp());
        String date = simpleDateFormat.format(new Date(time));

        tvRecDesc.setText("Date: " + date);
        tvRecQty.setText(""+item.getQuantity());
        tvRecTotal.setText(String.format("€%.2f", item.getTotal()));
        return item;
    }

    @Override
    public TableLayout getForeground() {
        return null;
    }

    @Override
    public TableLayout getBackground() {
        return null;
    }





}
