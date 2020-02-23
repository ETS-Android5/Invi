package com.aluminati.inventory.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TableRow;

import androidx.annotation.Nullable;

import com.aluminati.inventory.R;

@SuppressLint("AppCompatCustomView")
public class ToggleButton extends ImageView {

    private int toggleOffIcon, toggleOnIcon;
    private boolean isToggled;
    private float weight;
    private OnToggleButtonListener onToggleButtonListener;
    private boolean toggleMode;

    public interface OnToggleButtonListener {
        void isToggled(boolean isActive);
    }

    public ToggleButton(Context context) {
        super(context);
    }

    public ToggleButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0,0);

    }

    public ToggleButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs,0 ,0);
    }

    public ToggleButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        toggleMode = true;//default button can toggle
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ToggleButton,
                defStyleAttr, defStyleRes);

        try {
            toggleOffIcon = a.getResourceId(R.styleable.ToggleButton_toggleOffIcon, R.mipmap.ic_launcher);
            toggleOnIcon = a.getResourceId(R.styleable.ToggleButton_toggleOnIcon, R.mipmap.ic_launcher);
            weight = a.getFloat(R.styleable.ToggleButton_toggleButtonWeight, 0);
            toggleMode = a.getBoolean(R.styleable.ToggleButton_toggleMode, true);
            setImageResource(toggleOffIcon);
            setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT,weight));

        } finally {
            a.recycle();
        }

        this.setOnClickListener(view -> {
            isToggled = !isToggled;

            if(toggleMode) {
                if(onToggleButtonListener != null) {
                    onToggleButtonListener.isToggled(isToggled);
                }

                setImageResource(isToggled ? toggleOnIcon : toggleOffIcon);
            }
        });

    }

    public void setToggleOffIcon(int toggleOffIcon) {
        this.toggleOffIcon = toggleOffIcon;
        setImageResource(toggleOffIcon);
    }

    public void setToggleOnIcon(int toggleOnIcon) {
        this.toggleOnIcon = toggleOnIcon;
    }

    public void setToggleImages(int toggleOffIcon, int toggleOnIcon) {
        this.toggleOffIcon = toggleOffIcon;
        this.toggleOnIcon = toggleOnIcon;
        setImageResource(toggleOffIcon);
    }

    public boolean isToggled() {
        return isToggled;
    }

    public void setToggleModeOn(boolean toggleMode) {
        this.toggleMode = toggleMode;
    }
    
    public void setOnToggleButtonListener(OnToggleButtonListener onToggleButtonListener) {
        this.onToggleButtonListener = onToggleButtonListener;

    }

    public boolean equals(Object obj) {
        if(this == obj) return true;

        if(obj instanceof ToggleButton) {
            return (this.getId() == ((ToggleButton)obj).getId());
        }

        return false;
    }
}
