package com.aluminati.inventory.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.binders.IBinder;

import java.util.List;

/*
 *  Generic Adapter to handle any model type
 *  Please see Purchase Binder as an example of have how to implement
 *  IBinder
 */
public class ItemAdapter<T> extends RecyclerView.Adapter<ItemAdapter<T>.ViewHolder> {
    private final OnItemClickListener<T> listener;
    private List<T> items;
    private Context context;
    private IBinder<T> iBinder;
    private int layout;
    public ItemAdapter(List<T> Items, OnItemClickListener listener, IBinder<T> iBinder, int layout,
                       Context context) {
        this.items = Items;
        this.listener = listener;
        this.context = context;
        this.iBinder = iBinder;
        this.layout = layout;
    }

    @NonNull
    @Override
    public ItemAdapter<T>.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new ItemAdapter<T>.ViewHolder(itemView, iBinder);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public T getItem(int position) {
        return items.get(position);
    }


    public T removeItem(int position) {
        return removeItem(items.get(position));
    }

    public T removeItem(T Item) {
        items.remove(Item);
        notifyDataSetChanged();
        return Item;
    }

    public void resortItems(boolean asc) {
        //sort items here. ASC or DESC
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TableLayout viewForeground, viewBackground;
        private IBinder<T> binder;

        public ViewHolder(View view, IBinder<T> binder) {
            super(view);
            this.binder = binder;
            this.binder.initViews(view);
            this.viewForeground = binder.getForeground();
            this.viewBackground = binder.getBackground();
        }

        public void bind(final T item, final OnItemClickListener<T> listener){
            binder.bind(item, context);
            itemView.setOnClickListener(view -> listener.onItemClick(item));
        }

        public View getForeground() {return viewForeground;}
        public View getBackground() {return viewBackground;}
    }


    /**
     * Apply a filtered list - Used during search
     * @param Items
     */
    public void applyFilter(List<T> Items) {
        this.items = Items;
        //Collections.sort(this.Items);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }
}