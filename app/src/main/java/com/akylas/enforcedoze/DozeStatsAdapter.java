package com.akylas.enforcedoze;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying Doze stats cards
 */
public class DozeStatsAdapter extends RecyclerView.Adapter<DozeStatsAdapter.ViewHolder> {
    
    private List<DozeStatsCard> cards = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView supportingText;
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            supportingText = itemView.findViewById(R.id.supportingText);
            image = itemView.findViewById(R.id.image);
        }

        public void bind(DozeStatsCard card) {
            title.setText(card.getTitle());
            supportingText.setText(card.getDescription());
            image.setImageDrawable(card.getDrawable());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.material_small_image_card_custom, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(cards.get(position));
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void addCard(DozeStatsCard card) {
        cards.add(card);
        notifyItemInserted(cards.size() - 1);
    }

    public void clearAll() {
        int size = cards.size();
        cards.clear();
        notifyItemRangeRemoved(0, size);
    }
}
