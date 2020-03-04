package com.aluminati.inventory.binders;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.model.BaseItem;
import com.bumptech.glide.Glide;

public class BaseBinder implements IBinder<BaseItem> {
    private TextView itemTitle, itemDescription, itemPrice;
    private View itemView;
    private ImageView itemImg;
    private TableLayout viewForeground, viewBackground;

    @Override
    public View initViews(View view) {
        this.itemView = view;
        itemTitle = itemView.findViewById(R.id.itemTitle);
        itemDescription = itemView.findViewById(R.id.itemDescription);
        itemPrice = itemView.findViewById(R.id.itemPrice);
        itemImg = itemView.findViewById(R.id.itemImg);

        viewForeground = itemView.findViewById(R.id.foreground_view);
        viewBackground = itemView.findViewById(R.id.background_view);
        return itemView;
    }

    @Override
    public BaseItem bind(BaseItem item, Context context) {
        itemTitle.setText(item.getTitle());
        itemDescription.setText(item.getDescription());

        itemPrice.setText("" + item.getPrice());

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
