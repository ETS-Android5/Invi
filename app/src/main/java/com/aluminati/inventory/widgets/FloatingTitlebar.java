package com.aluminati.inventory.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;

import androidx.cardview.widget.CardView;

import com.aluminati.inventory.R;

/**
 * Floating Titlebar. This widget is a lot more flexible
 * than the standard titlebar and can expand, coded
 * against easier than standard android titlebar.
 */
public class FloatingTitlebar extends LinearLayout {
    private ImageView leftButton, rightButton;
    private EditText searchField;
    private CardView card;
    private boolean leftActive, rightActive, toggleActive, leftToggleOff, rightToggleOff;
    private ToggleListener toggleListener;
    private int leftButtonIcon, leftToggleIcon, rightToggleIcon, rightButtonIcon;
    private SearchTextChangeListener searchTextChangeListener;

    /**
     * Implement to capture text from title search bar
     */
    public interface SearchTextChangeListener {
        void onTextChanged(String searchText);
    }

    /**
     * Implement to capture button press on toggle
     */
    public interface ToggleListener {
       void onLeftButtonToggle(boolean isActive);
       void onRightButtonToggle(boolean isActive);
    }

    public FloatingTitlebar(Context context) {
        super(context);
    }

    public FloatingTitlebar(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FloatingTitlebar,
                0, 0);

        card = new CardView(context);
        TableRow row = new TableRow(context, attrs);

        leftButton = new ImageView(context);
        rightButton = new ImageView(context);
        searchField = new EditText(context);

        ViewGroup.LayoutParams btnW = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.1f);
        ViewGroup.LayoutParams editW = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f);

        leftButton.setLayoutParams(btnW);
        rightButton.setLayoutParams(btnW);
        searchField.setLayoutParams(editW);

        try {
            leftButtonIcon = a.getResourceId(R.styleable.FloatingTitlebar_leftDrawable, R.mipmap.ic_launcher);
            rightButtonIcon = a.getResourceId(R.styleable.FloatingTitlebar_rightDrawable, R.mipmap.ic_launcher);

            leftToggleIcon = a.getResourceId(R.styleable.FloatingTitlebar_leftDrawableToggle, 0);
            rightToggleIcon = a.getResourceId(R.styleable.FloatingTitlebar_rightDrawableToggle, 0);

            leftButton.setImageResource(leftButtonIcon);
            rightButton.setImageResource(rightButtonIcon);;

            searchField.setHint(a.getString(R.styleable.FloatingTitlebar_searchHint));
            card.setCardElevation(a.getFloat(R.styleable.FloatingTitlebar_setElevation, 15f));
            card.setRadius(a.getFloat(R.styleable.FloatingTitlebar_setCornerRadius, 15f));

        } finally {
            a.recycle();
        }
        row.addView(leftButton);
        row.addView(searchField);
        row.addView(rightButton);

        ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        row.setGravity(Gravity.CENTER_HORIZONTAL |Gravity.CENTER_VERTICAL);
        card.setLayoutParams(lp);

        loadListeners();
        card.addView(row);

        addView(card);
        setPadding(10,5,10,5);

        setBackgroundColor(Color.TRANSPARENT);
    }

    private void loadListeners() {
        leftButton.setOnClickListener(view -> {
            leftActive = !leftActive;
            if(toggleActive && !leftToggleOff && leftToggleIcon != 0) {
                leftButton.setImageResource(leftActive ? leftToggleIcon : leftButtonIcon);
            }
            if(toggleListener != null) {
                toggleListener.onLeftButtonToggle(leftActive);
            }

        });

        rightButton.setOnClickListener(view -> {
            rightActive = !rightActive;
            if(toggleActive && !rightToggleOff && rightToggleIcon != 0) {
                rightButton.setImageResource(rightActive ? rightToggleIcon : rightButtonIcon);
            }
            if(toggleListener != null) {
                toggleListener.onRightButtonToggle(rightActive);
            }
        });

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(searchTextChangeListener != null) {
                    searchTextChangeListener.onTextChanged(charSequence.toString());
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    /**
     * Show Floating Titlebar
     */
    public void showFloatingTitlebar() {
        this.setVisibility(View.VISIBLE);
    }

    /**
     * Hide FLoatingTitlebar
     */
    public void hideFloatingTitle() {
        this.setVisibility(View.GONE);
    }

    /**
     * Setting toggle to true allows button icons to be swapped
     * when button is clicked. True indicates the second state is active
     * set second drawable leftDrawableToggle rightDrawableToggle in layout
     *
     * False does not swap icons on press
     * @param toggleActive
     */
    public void setToggleActive(boolean toggleActive) {
        this.toggleActive = toggleActive;
    }

    /**
     * If true toggle icon will not be shown
     * @param leftToggleOff
     */
    public void setLeftToggleOff(boolean leftToggleOff) {
        this.leftToggleOff = leftToggleOff;
    }

    /**
     * If true toggle icon will not be shown
     * @param rightToggleOff
     */
    public void setRightToggleOff(boolean rightToggleOff) {
        this.rightToggleOff = rightToggleOff;
    }

    /**
     * The the ToggleListener to capture when button has been clicked
     * @param toggleListener
     */
    public void setOnToggleListener(ToggleListener toggleListener) {
        this.toggleListener = toggleListener;
    }

    /**
     * Set SearchTextChangeListener to capture when the text has been typed
     * @param searchTextChangeListener
     */
    public void setOnSearchTextChangeListener(SearchTextChangeListener searchTextChangeListener) {
        this.searchTextChangeListener = searchTextChangeListener;
    }

    /**
     * Carview of floating titlebar
     * @return
     */
    public CardView getCard() {
        return card;
    }

    /**
     * Set corner radius for CardView
     * @param radius
     */
    public void setCornerRadius(float radius) {
        card.setRadius(radius);
    }

    /**
     * Set left button drawable programmatically
     * @param drawable
     */
    public void setLeftButtonIcon(int drawable) {
        leftButton.setImageResource(drawable);
    }

    /**
     * Set left toggle button drawable programmatically
     * @param drawable
     */
    public void setLeftToggleButtonIcon(int drawable) {
        leftToggleIcon = drawable;
    }

    /**
     * Set right button drawable programmatically
     * @param drawable
     */
    public void setRightButtonIcon(int drawable) {
        rightButton.setImageResource(drawable);
    }

    /**
     * Set right toggle button drawable programmatically
     * @param drawable
     */
    public void setRightToggleIcon(int drawable) {
        rightToggleIcon = drawable;
    }


    /**
     * Set hint for search field
     * @param hint
     */
    public void setSearchFieldHint(String hint) {
        searchField.setHint(hint);
    }

    /**
     * Get left button as ImageView
     * @return
     */
    public ImageView getLeftButton() {
        return leftButton;
    }


    /**
     * Get right button as ImageView
     * @return
     */
    public ImageView getRightButton() {
        return rightButton;
    }

    /**
     * Get search field as EditText view
     * @return
     */
    public EditText getSearchField() {
        return searchField;
    }

}
