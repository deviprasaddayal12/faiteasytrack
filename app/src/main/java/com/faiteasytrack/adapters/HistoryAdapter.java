package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.faiteasytrack.R;
import com.faiteasytrack.models.HistoryModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnHistorySelectedListener {
        void onHistorySelected(int position, HistoryModel historyModel);
    }

    private Context gContext;
    private ArrayList<HistoryModel> historyModels;
    private OnHistorySelectedListener onHistorySelectedListener;

    public HistoryAdapter(Context gContext, ArrayList<HistoryModel> historyModels, OnHistorySelectedListener onHistorySelectedListener) {
        this.gContext = gContext;
        this.historyModels = historyModels;
        this.onHistorySelectedListener = onHistorySelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HistoryItemView(LayoutInflater.from(gContext).inflate(R.layout.row_friend_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        HistoryModel historyModel = historyModels.get(position);
    }

    @Override
    public int getItemCount() {
        return historyModels.size();
    }

    class HistoryItemView extends RecyclerView.ViewHolder {

        public HistoryItemView(@NonNull View itemView) {
            super(itemView);
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onHistorySelectedListener != null)
                        onHistorySelectedListener.onHistorySelected(getAdapterPosition(), historyModels.get(getAdapterPosition()));
                }
            });
        }
    }
}
