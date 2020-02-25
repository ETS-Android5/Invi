package com.aluminati.inventory.adapters.swipelisteners;


import android.graphics.Canvas;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.adapters.ItemAdapter;

public class ItemSwipe extends ItemTouchHelper.SimpleCallback {
    private static final String TAG = ItemSwipe.class.getSimpleName();

    private ItemHelperListener listener;
    private RecyclerView recyclerView;

    public ItemSwipe(int dragDirs, int swipeDirs, ItemHelperListener listener) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        this.recyclerView = recyclerView;
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return super.getMovementFlags(recyclerView, viewHolder);
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        onSelectUpdate(viewHolder);
    }

    private View  onSelectUpdate(RecyclerView.ViewHolder viewHolder) {
        View fv = null;
        if(viewHolder != null && viewHolder instanceof ItemAdapter.ViewHolder) {
            fv = ((ItemAdapter.ViewHolder) viewHolder).getForeground();
            getDefaultUIUtil().onSelected(fv);
        }

        return fv;
    }

    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                        int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        Log.d(TAG, "clearView ");
        View fv = onSelectUpdate(viewHolder);
        getDefaultUIUtil().clearView(fv);
    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int state, boolean active) {
        Log.d(TAG, "onChildDrawOver " + dX + " " + dY);
        View fv = onSelectUpdate(viewHolder);

        getDefaultUIUtil().onDrawOver(c, recyclerView, fv, dX, dY, state, active);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder, float dX, float dY,
                            int state, boolean active) {
        Log.d(TAG, "onChildDraw " + dX + " " + dY);

        View fv = onSelectUpdate(viewHolder);
        getDefaultUIUtil().onDraw(c, recyclerView, fv, dX, dY, state, active);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    @Override
    public int convertToAbsoluteDirection(int flags, int layoutDirection) {
        return super.convertToAbsoluteDirection(flags, layoutDirection);
    }

    public interface ItemHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }

}