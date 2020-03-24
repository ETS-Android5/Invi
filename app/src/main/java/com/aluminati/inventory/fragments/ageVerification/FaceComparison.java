package com.aluminati.inventory.fragments.ageVerification;

import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.utils.Utils;
import com.aluminati.inventory.payments.ui.PaymentsFrag;
import com.google.firebase.auth.FirebaseAuth;

public class FaceComparison extends AppCompatActivity implements View.OnClickListener{

    private EditText day, month, year;
    private String date = "";
    private PaymentsFrag paymentsFrag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.id_verification);

        day = findViewById(R.id.day);
        day.setOnClickListener(this);
        month = findViewById(R.id.month);
        month.setOnClickListener(this);
        year = findViewById(R.id.year);
        year.setOnClickListener(this);

        findViewById(R.id.verify_identity).setOnClickListener(this);

    }

    private void openDatePicker(){
        DatePicker datePicker = new DatePicker(this);
                   datePicker.setOnClickListener(click -> {
                       day.setText(datePicker.getDayOfMonth());
                       month.setText(datePicker.getDayOfMonth());
                       year.setText(datePicker.getYear());
                       date = day.getText().toString() + "-" + month.getText().toString() + "-" + year.getText().toString();
                   });
    }

    private void onResultRecieved(String result){

        String[] displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName().split(" ");
        int detailsMatch = 0;


        if(!result.isEmpty()){
            String[] results = result.split("#");
            for(String res : results){
                if(res.toLowerCase().equals(displayName[0].toLowerCase())){
                    detailsMatch++;
                }else if(res.toLowerCase().equals(displayName[1].toLowerCase())){
                    detailsMatch++;
                }else if(res.toLowerCase().equals(date.toLowerCase())){
                    detailsMatch++;
                }
            }
        }

        if(detailsMatch == 3){
            Utils.makeSnackBar("User verified", day, this);
        }else{
            Utils.makeSnackBar("Failed to verified user", day, this);
        }
    }

    private void bindFrag(Fragment frag){
        if(frag instanceof PaymentsFrag){
            ((PaymentsFrag)frag).setSendBackResult(this::onResultRecieved);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSupportFragmentManager().getFragments().contains(paymentsFrag)){
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.day:
            case R.id.month:
            case R.id.year:{
                openDatePicker();
                break;
            }
            case R.id.verify_identity:{

                paymentsFrag = new PaymentsFrag();
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.verify_id_layout, paymentsFrag, "verify_frag")
                        .addToBackStack("verify_frag")
                        .commit();

                bindFrag(paymentsFrag);
                break;
            }
        }
    }
}
