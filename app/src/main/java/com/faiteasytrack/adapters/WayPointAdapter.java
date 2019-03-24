package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.customclasses.ETLatLng;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class WayPointAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnWayPointSelectedListener {
        void onWayPointSelected(int position, ETLatLng etLatLng);
    }

    private Context gContext;
    private ArrayList<ETLatLng> etLatLngs;
    private OnWayPointSelectedListener onWayPointSelectedListener;

    public WayPointAdapter(Context gContext, ArrayList<ETLatLng> etLatLngs, OnWayPointSelectedListener onWayPointSelectedListener) {
        this.gContext = gContext;
        this.etLatLngs = etLatLngs;
        this.onWayPointSelectedListener = onWayPointSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new WaypointItemView(LayoutInflater.from(gContext).inflate(R.layout.row_waypoint_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ETLatLng etLatLng = etLatLngs.get(position);

        WaypointItemView waypointItemView = (WaypointItemView) holder;
        waypointItemView.tvWayPointName.setText(etLatLng.getLatLng().toString());
    }

    @Override
    public int getItemCount() {
        return etLatLngs.size();
    }

    class WaypointItemView extends RecyclerView.ViewHolder {

        private TextView tvWayPointName;

        public WaypointItemView(@NonNull View itemView) {
            super(itemView);

            tvWayPointName = itemView.findViewById(R.id.tv_waypoint_name);

            itemView.findViewById(R.id.btn_assign_parent).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onWayPointSelectedListener != null)
                        onWayPointSelectedListener.onWayPointSelected(getAdapterPosition(), etLatLngs.get(getAdapterPosition()));
                }
            });
        }
    }
}
