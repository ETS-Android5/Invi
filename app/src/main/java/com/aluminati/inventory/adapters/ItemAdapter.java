package com.aluminati.inventory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.model.BaseItem;
import com.aluminati.inventory.model.RentalItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final OnItemClickListener listener;
    private List<? extends BaseItem> items;
    private Context context;
    public ItemAdapter(List<? extends BaseItem> Items, OnItemClickListener listener, Context context) {
        this.items = Items;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new ItemAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public BaseItem getItem(int position) {
        return items.get(position);
    }


    public BaseItem removeItem(int position) {
        return removeItem(items.get(position));
    }

    public BaseItem removeItem(BaseItem Item) {
        items.remove(Item);
        notifyDataSetChanged();
        return Item;
    }

    public void resortItems(boolean asc) {
        //sort items here. ASC or DESC
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //TextView fields here
        TextView itemTitle, itemDescription, itemPrice;
        View itemView;
        ImageView itemImg;
        public TableLayout viewForeground, viewBackground;

        public ViewHolder(View view) {
            super(view);
            this.itemView = view;
//            //init view here
            itemTitle = view.findViewById(R.id.itemTitle);
            itemDescription = view.findViewById(R.id.itemDescription);
            itemPrice = view.findViewById(R.id.itemPrice);
            itemImg = view.findViewById(R.id.itemImg);

            viewForeground = view.findViewById(R.id.foreground_view);
            viewBackground = view.findViewById(R.id.background_view);
        }

        public void bind(final BaseItem item, final OnItemClickListener listener){
            //set data here

            itemTitle.setText(item.getTitle());
            itemDescription.setText(item.getDescription());
            boolean isRental = item instanceof RentalItem;

            itemPrice.setText(String.format("€%.2f ", item.getPrice())
                    + (isRental
                    ? "Per " + ((RentalItem) item).getUnitType()
                    : ""));

            Glide.with(context)
                    .load(item.getImgLink())
                    .into(itemImg);

            itemView.setOnClickListener(view -> listener.onItemClick(item));
        }
    }

    /**
     * Apply a filtered list - Used during search
     * @param Items
     */
    public void applyFilter(List<BaseItem> Items) {
        this.items = Items;
        //Collections.sort(this.Items);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(BaseItem item);
    }
}