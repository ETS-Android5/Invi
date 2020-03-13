package com.aluminati.inventory.binders;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.Utils;
import com.aluminati.inventory.fragments.rental.RentalItem;
import com.bumptech.glide.Glide;

/*
 * This class will be injected in ItemAdapter
 */
public class RentalBinder implements IBinder<RentalItem> {
    private TextView itemTitle, itemDescription, itemPrice, itemCurrentRentalCost, tvRentalUnit;
    private View itemView;
    private ImageView itemImg;
    private TableLayout viewForeground, viewBackground;

    public View initViews(View view) {
        this.itemView = view;
        itemTitle = itemView.findViewById(R.id.itemTitle);
        itemDescription = itemView.findViewById(R.id.itemDescription);
        itemPrice = itemView.findViewById(R.id.itemPrice);
        itemImg = itemView.findViewById(R.id.itemImg);
        tvRentalUnit = itemView.findViewById(R.id.tvRentalUnit);
        itemCurrentRentalCost = itemView.findViewById(R.id.itemCurrentRentalCost);

        viewForeground = itemView.findViewById(R.id.foreground_view);
        viewBackground = itemView.findViewById(R.id.background_view);
        return itemView;
    }


    public RentalItem bind(RentalItem item, Context context) {
        itemTitle.setText(item.getTitle());
        itemDescription.setText(item.getDescription());
        tvRentalUnit.setText("Price Per " + item.getUnitType());

        itemPrice.setText(String.format("€%.2f", item.getPrice()));
        double balance = Utils.getRentalCharge(item);
        itemCurrentRentalCost.setText(String.format("€%.2f",balance ));
        Log.d(RentalBinder.class.getSimpleName(), "Rental Cost:" + balance);

        Glide.with(context)
                .load(item.getImgLink())
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

    public TextView getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(TextView itemTitle) {
        this.itemTitle = itemTitle;
    }

    public TextView getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(TextView itemDescription) {
        this.itemDescription = itemDescription;
    }

    public TextView getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(TextView itemPrice) {
        this.itemPrice = itemPrice;
    }

    public View getItemView() {
        return itemView;
    }

    public void setItemView(View itemView) {
        this.itemView = itemView;
    }

    public ImageView getItemImg() {
        return itemImg;
    }

    public void setItemImg(ImageView itemImg) {
        this.itemImg = itemImg;
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
