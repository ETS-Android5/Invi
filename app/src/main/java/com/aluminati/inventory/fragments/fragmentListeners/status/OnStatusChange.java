package com.aluminati.inventory.fragments.fragmentListeners.status;


public class OnStatusChange
{
    private OnStatusChangeListener listener;

    private int value;

    public void setOnIntegerChangeListener(OnStatusChangeListener listener)
    {
        this.listener = listener;
    }

    public int get()
    {
        return value;
    }

    public void set(int value)
    {
        this.value = value;

        if(listener != null)
        {
            listener.onStatusChanged(value);
        }
    }
}