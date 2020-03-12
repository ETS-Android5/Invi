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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;

import com.aluminati.inventory.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Floating Titlebar. This widget is a lot more flexible
 * than the standard titlebar and can expand, coded
 * against easier than standard android titlebar.
 */
public class FloatingTitlebar extends LinearLayout {
    private ToggleButton leftButton, rightButton;
    private EditText searchField;
    private TextView titleText;
    private CardView card;
    private ToggleListener toggleListener;
    private SearchTextChangeListener searchTextChangeListener;
    private List<ToggleButton> toggleButtonList;

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

        toggleButtonList = new ArrayList<>();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.FloatingTitlebar,
                0, 0);

        card = new CardView(context);
        TableRow row = new TableRow(context, attrs);

        leftButton = new ToggleButton(context, attrs);
        rightButton = new ToggleButton(context, attrs);
        searchField = new EditText(context);
        searchField.setMaxLines(1);
        titleText = new TextView(context);
        titleText.setMaxLines(1);


        ViewGroup.LayoutParams btnW = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.1f);
        ViewGroup.LayoutParams editW = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f);

        leftButton.setLayoutParams(btnW);
        rightButton.setLayoutParams(btnW);
        searchField.setLayoutParams(editW);
        titleText.setLayoutParams(editW); //same with as search field
        titleText.setVisibility(View.GONE);//not visible by default

        try {
            leftButton.setToggleImages(a.getResourceId(R.styleable.FloatingTitlebar_leftToggleOffIcon, R.mipmap.ic_launcher),
                    a.getResourceId(R.styleable.FloatingTitlebar_leftToggleOnIcon, R.mipmap.ic_launcher));
            rightButton.setToggleImages(a.getResourceId(R.styleable.FloatingTitlebar_rightToggleOffIcon, R.mipmap.ic_launcher),
                    a.getResourceId(R.styleable.FloatingTitlebar_rightToggleOnIcon, R.mipmap.ic_launcher));

//            titleText.setTextColor(a.getInt(R.styleable.FloatingTitlebar_titleTextColor, Color.BLACK));
            titleText.setMinHeight(a.getInt(R.styleable.FloatingTitlebar_titleTextMinHeight, 30));
            titleText.setHint(a.getString(R.styleable.FloatingTitlebar_titleText));

            searchField.setHint(a.getString(R.styleable.FloatingTitlebar_searchHint));
            card.setCardElevation(a.getFloat(R.styleable.FloatingTitlebar_setElevation, 15f));
            card.setRadius(a.getFloat(R.styleable.FloatingTitlebar_setCornerRadius, 15f));

        } finally {
            a.recycle();
        }
        row.addView(leftButton);
        row.addView(searchField);
        row.addView(titleText);
        row.addView(rightButton);

        ViewGroup.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        row.setGravity(Gravity.CENTER_HORIZONTAL |Gravity.CENTER_VERTICAL);
        card.setLayoutParams(lp);

        loadListeners();
        card.addView(row);

        addView(card);
        setPadding(10,10,10,10);

        setBackgroundColor(Color.TRANSPARENT);
    }

    private void loadListeners() {
        leftButton.setOnToggleButtonListener(isActive -> {
            toggleListener.onLeftButtonToggle(isActive);
        });

        rightButton.setOnToggleButtonListener(isActive -> {
            toggleListener.onRightButtonToggle(isActive);
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

    public void addToggleButton(ToggleButton toggleButton) {
        toggleButtonList.add(toggleButton);
    }

    public boolean removeToggleButton(ToggleButton toggleButton) {
        return toggleButtonList.remove(toggleButton);
    }

    public void clearToggleButtons() {
        toggleButtonList.clear();
    }


    /**
     * Set text of title bar
     * @param txt
     */
    public void setTitleText(String txt) {
        titleText.setText(txt);
    }

    /**
     * Show Title Text Bar. Hides search bar when visible
     */
    public void showTitleTextBar() {
        titleText.setVisibility(View.VISIBLE);
        searchField.setVisibility(View.GONE);
    }

    /**
     * Show Search Bar. Hides Title Text Bar when visible
     */
    public void showSearchBar() {
        titleText.setVisibility(View.GONE);
        searchField.setVisibility(View.VISIBLE);
    }

    public void showButtonsOnly(boolean buttonsOnly) {
        int v = buttonsOnly ? View.GONE : View.VISIBLE;
        titleText.setVisibility(v);
        searchField.setVisibility(v);
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
        leftButton.setToggleModeOn(toggleActive);
        rightButton.setToggleModeOn(toggleActive);
    }

    /**
     * If false button will not toggle
     * @param leftToggleOn
     */
    public void setLeftToggleOn(boolean leftToggleOn) {
        leftButton.setToggleModeOn(leftToggleOn);
    }

    /**
     * If false toggle icon will not be shown
     * @param rightToggleOn
     */
    public void setRightToggleOn(boolean rightToggleOn) {
        rightButton.setToggleModeOn(rightToggleOn);
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
     * Set left toggle off icon drawable programmatically
     * @param drawable
     */
    public void setLeftToggleOffIcon(int drawable) {
        leftButton.setToggleOffIcon(drawable);
    }

    /**
     * Set left toggle on icon drawable programmatically
     * @param drawable
     */
    public void setLeftToggleOnIcon(int drawable) {
        leftButton.setToggleOnIcon(drawable);
    }

    public void setLeftToggleIcons(int toggleOffIcon, int toggleOnIcon) {
        leftButton.setToggleImages(toggleOffIcon, toggleOnIcon);
    }

    /**
     * Set right toggle off icon drawable programmatically
     * @param drawable
     */
    public void setRightToggleOffIcon(int drawable) {
        rightButton.setToggleOffIcon(drawable);
    }

    /**
     * Set right toggle off icon drawable programmatically
     * @param drawable
     */
    public void setRightToggleOnIcon(int drawable) {
        rightButton.setToggleOnIcon(drawable);
    }

    public void setRightToggleIcons(int toggleOffIcon, int toggleOnIcon) {
        rightButton.setToggleImages(toggleOffIcon, toggleOnIcon);
    }


    /**
     * Set hint for search field
     * @param hint
     */
    public void setSearchFieldHint(String hint) {
        searchField.setHint(hint);
    }

    /**
     * Get left button as ToggleButton
     * @return
     */
    public ToggleButton getLeftButton() {
        return leftButton;
    }


    /**
     * Get right button as ToggleButton
     * @return
     */
    public ToggleButton getRightButton() {
        return rightButton;
    }

    /**
     * Get search field as EditText view
     * @return
     */
    public EditText getSearchField() {
        return searchField;
    }

    /**
     * Get Title Text as TextView
     * @return
     */
    public TextView getTitleText() {
        return titleText;
    }

}
