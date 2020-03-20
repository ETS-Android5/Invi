package com.aluminati.inventory.fragments.googleMaps.customMarker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aluminati.inventory.R;
import com.aluminati.inventory.fragments.tesco.objects.StandardOpeningHours;

import java.util.ArrayList;

public class CustomMarkerAdapter extends RecyclerView.Adapter<CustomMarkerAdapter.OpeningTimesViewHolder> implements View.OnClickListener{


        private ArrayList<StandardOpeningHours> dataSet;

        public CustomMarkerAdapter(ArrayList<StandardOpeningHours> data) {
            this.dataSet = data;
        }

        @Override
        public OpeningTimesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.opening_times_card, parent, false);
            return new OpeningTimesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OpeningTimesViewHolder holder, int position) {
            holder.day.setText(dataSet.get(position).getDay());
            holder.openingTimes.setText(dataSet.get(position).getOpenTime());
            holder.closingTimes.setText(dataSet.get(position).getCloseTime());
        }


        @Override
        public int getItemCount() {
            return dataSet.size();
        }

        @Override
        public void onClick(View view) {
        }


        static class OpeningTimesViewHolder extends RecyclerView.ViewHolder {

            TextView day;
            TextView openingTimes;
            TextView closingTimes;

            OpeningTimesViewHolder(View itemView) {
                super(itemView);
                this.day = itemView.findViewById(R.id.opening_day);
                this.openingTimes = itemView.findViewById(R.id.opening_times);
                this.closingTimes = itemView.findViewById(R.id.closing_times);
            }
        }
}
