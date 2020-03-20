package com.aluminati.inventory.fragments.summary.purchaseCategories;

import android.graphics.Color;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aluminati.inventory.R;
import com.aluminati.inventory.firestore.UserFetch;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class PurchaseCategories extends Fragment {

    private static final String TAG = PurchaseCategories.class.getName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(getResources().getLayout(R.layout.purchase_categories), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PieChartView pieChartView = view.findViewById(R.id.chart);

        List<SliceValue> pieData = new ArrayList<>();

        UserFetch.getUser(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .addOnSuccessListener(success -> {
                    Map<String,Long> cats = (Map<String, Long>) success.get("items_categories");
                    if(cats != null){
                        float total = getTotal(cats);
                        float slice = 0;float result = 0;
                        Set<String> keys = cats.keySet();
                        for(String key : keys){
                            if(cats.get(key) != null) {
                                slice = cats.get(key);
                                result = ((slice/total)*360);
                                Log.i(TAG, "Result " + result + " slice " + slice + " total " + total);
                                pieData.add(new SliceValue(result, randColor()).setLabel(key.concat(":").concat(Float.toString(result))));
                            }
                        }
                        if(!pieData.isEmpty()){
                            view.findViewById(R.id.purchase_categories_placeholder).setVisibility(View.INVISIBLE);
                            PieChartData pieChartData = new PieChartData(pieData);
                            pieChartData.setHasLabels(true).setValueLabelTextSize(14);
                            pieChartData.setHasLabelsOutside(true);
                            pieChartData.setHasCenterCircle(true).setCenterText1("Summary").setCenterText1FontSize(20).setCenterText1Color(Color.parseColor("#0097A7"));
                            pieChartView.setPieChartData(pieChartData);
                        }
                    }

                })
                .addOnFailureListener(failure -> {
                    Log.i(TAG, "Failed to get user",failure);
                });




    }

    private int randColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));

    }

    private long getTotal(Map<String,Long> cat){
        long total = 0;
        Set<String> keys = cat.keySet();
        for(String key : keys){
            if(cat.get(key) != null) {
                total += cat.get(key);
            }
        }
        return total;
    }
}
