package com.example.emim.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emim.R;

import java.util.List;

public class CoordinatesAdapter extends RecyclerView.Adapter<CoordinatesAdapter.ViewHolder> {

    public interface OnRemoveClick { void onRemove(int position); }

    private final List<Coordinate> data;
    private final OnRemoveClick listener;

    public CoordinatesAdapter(List<Coordinate> data, OnRemoveClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_coordinate, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Coordinate c = data.get(pos);
        h.tvLat.setText("Lat: " + c.latitude);
        h.tvLon.setText("Lon: " + c.longitude);
        h.btnRemove.setOnClickListener(v -> listener.onRemove(pos));
    }

    @Override public int getItemCount() { return data.size(); }

    public void add(Coordinate c) {
        data.add(c);
        notifyItemInserted(data.size()-1);
    }

    public void remove(int pos) {
        data.remove(pos);
        notifyItemRemoved(pos);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLat, tvLon;
        ImageButton btnRemove;
        ViewHolder(View v) {
            super(v);
            tvLat       = v.findViewById(R.id.tvItemLatitude);
            tvLon       = v.findViewById(R.id.tvItemLongitude);
            btnRemove   = v.findViewById(R.id.btnRemoveCoord);
        }
    }
}
