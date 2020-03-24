package com.aluminati.inventory.binders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.receipt.ReceiptListItem;
import com.bumptech.glide.Glide;

/*
 * This class will be injected in ItemAdapter
 */
public class ReceiptDialogBinder implements IBinder<ReceiptListItem> {
    private TextView itemTitle, itemCurrentRentalCost, tvQuantity;
    private View itemView;
    private ImageView itemImg;
    private TableLayout viewForeground, viewBackground;

    public View initViews(View view) {
        this.itemView = view;
        itemTitle = itemView.findViewById(R.id.itemTitle);
        tvQuantity = itemView.findViewById(R.id.itemPrice);
        itemImg = itemView.findViewById(R.id.itemImg);
        itemCurrentRentalCost = itemView.findViewById(R.id.itemCurrentRentalCost);

        viewForeground = itemView.findViewById(R.id.foreground_view);
        viewBackground = itemView.findViewById(R.id.background_view);
        return itemView;
    }


    public ReceiptListItem bind(ReceiptListItem item, Context context) {
        itemTitle.setText(item.getTitle());
        tvQuantity.setText("" + item.getQuantity());
        itemCurrentRentalCost.setText(String.format("€%.2f",item.getPrice() ));

        Glide.with(context)
                .load(item.getImgurl())
                .into(itemImg);

        return item;
    }

    @Override
    public TableLayout getForeground() {
        return getViewForeground();
    }

    @Override
    public TableLayout getBackground() {
        return getViewBackground();
    }

    public TableLayout getViewForeground() {
        return viewForeground;
    }

    public void setViewForeground(TableLayout viewForeground) {
        this.viewForeground = viewForeground;
    }

    public TableLayout getViewBackground() {
        return viewBackground;
    }

    public void setViewBackground(TableLayout viewBackground) {
        this.viewBackground = viewBackground;
    }
}
