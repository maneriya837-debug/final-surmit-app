package com.example.finalsurmitapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ShelterAdapter extends RecyclerView.Adapter<ShelterAdapter.ShelterViewHolder> {

    private List<Shelter> shelterList;

    public ShelterAdapter(List<Shelter> shelterList) {
        this.shelterList = shelterList;
    }

    @NonNull
    @Override
    public ShelterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shelter, parent, false);
        return new ShelterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShelterViewHolder holder, int position) {
        Shelter shelter = shelterList.get(position);
        holder.tvName.setText(shelter.getName());
        holder.tvStatus.setText("Status: " + shelter.getStatus());
    }

    @Override
    public int getItemCount() {
        return shelterList.size();
    }

    static class ShelterViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvStatus;

        public ShelterViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvShelterName);
            tvStatus = itemView.findViewById(R.id.tvShelterStatus);
        }
    }
}