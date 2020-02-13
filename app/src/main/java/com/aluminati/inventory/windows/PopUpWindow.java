package com.aluminati.inventory.windows;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;

public class PopUpWindow extends Fragment {

    private PopupWindow POPUP_WINDOW_SCORE = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.email_prompt, container, false);

        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;


        // Creating the PopupWindow
        //   POPUP_WINDOW_SCORE = new PopupWindow(this);
        POPUP_WINDOW_SCORE.setContentView(view);
        POPUP_WINDOW_SCORE.setWidth(width);
        POPUP_WINDOW_SCORE.setHeight(height);
        POPUP_WINDOW_SCORE.setFocusable(true);

        POPUP_WINDOW_SCORE.setBackgroundDrawable(null);

        POPUP_WINDOW_SCORE.showAtLocation(view, Gravity.CENTER, 1, 1);

        TextView txtMessage = (TextView) view.findViewById(R.id.layout_popup_txtMessage);

        Button butOne = (Button) view.findViewById(R.id.layout_popup_butOne);
        butOne.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Do Something

                //Close Window
                POPUP_WINDOW_SCORE.dismiss();
            }
        });

        Button butTwo = (Button) view.findViewById(R.id.layout_popup_butTwo);
        butTwo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Do Something

                //Close Window
                POPUP_WINDOW_SCORE.dismiss();
            }
        });
        return view;
    }



}
