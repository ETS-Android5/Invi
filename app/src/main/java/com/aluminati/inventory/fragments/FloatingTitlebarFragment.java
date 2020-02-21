package com.aluminati.inventory.fragments;

import android.os.Handler;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.widgets.FloatingTitlebar;

public abstract class FloatingTitlebarFragment extends Fragment implements FloatingTitlebar.ToggleListener,
        FloatingTitlebar.SearchTextChangeListener {
    protected FloatingTitlebar floatingTitlebar;
    protected DrawerLayout drawer;
    protected Handler handler;

    public FloatingTitlebarFragment() {}
    public FloatingTitlebarFragment(DrawerLayout drawer) {
        this.drawer = drawer;
    }
    public FloatingTitlebarFragment(DrawerLayout drawer, Handler handler) {
        this.handler = handler;
        this.drawer = drawer;
    }

    public void closeDrawer() {
        if(drawer != null) drawer.closeDrawer(GravityCompat.START);
    }

    public void openDrawer() {
        if(drawer != null) drawer.openDrawer(GravityCompat.START);
    }

    public FloatingTitlebar getFloatingTitlebar() {
        return floatingTitlebar;
    }

    protected  void setView(View root){
        floatingTitlebar = root.findViewById(R.id.floatingTitlebar);
        floatingTitlebar.setOnToggleListener(this);
    }

    @Override
    public void onLeftButtonToggle(boolean isActive) {
        openDrawer();
    }

    @Override
    public void onRightButtonToggle(boolean isActive) {

    }
}
