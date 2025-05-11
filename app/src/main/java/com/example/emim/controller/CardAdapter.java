package com.example.emim.controller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emim.R;
import com.example.emim.model.CardItem;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private List<CardItem> items;
    private OnItemClickListener listener;

    // Define interface
    public interface OnItemClickListener {
        void onItemClick(CardItem item);
    }

    public CardAdapter(List<CardItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView icon;

        public ViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
            icon = view.findViewById(R.id.icon);
        }

        public void bind(CardItem item, OnItemClickListener listener) {
            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }

    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardAdapter.ViewHolder holder, int position) {
        CardItem item = items.get(position);
        holder.title.setText(item.getTitle());
        holder.icon.setImageResource(item.getIconRes());
        holder.bind(item, listener);  // Bind the click listener here
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

