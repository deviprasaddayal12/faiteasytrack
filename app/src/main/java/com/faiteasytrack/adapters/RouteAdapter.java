package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.models.RouteModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RouteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnRouteSelectedListener {
        void onRouteSelected(int position, RouteModel routeModel);
    }

    private Context gContext;
    private ArrayList<RouteModel> routeModels;
    private OnRouteSelectedListener onRouteSelectedListener;

    public RouteAdapter(Context gContext, ArrayList<RouteModel> routeModels, OnRouteSelectedListener onRouteSelectedListener) {
        this.gContext = gContext;
        this.routeModels = routeModels;
        this.onRouteSelectedListener = onRouteSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RouteItemView(LayoutInflater.from(gContext).inflate(R.layout.row_route_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RouteModel routeModel = routeModels.get(position);

        RouteItemView routeItemView = (RouteItemView) holder;
        routeItemView.tvRouteSource.setText(String.valueOf(routeModel.getSource().getLatLng()));
        routeItemView.tvRouteDestination.setText(String.valueOf(routeModel.getDestination().getLatLng()));
        routeItemView.tvRouteWaypoints.setText(String.valueOf(routeModel.getWayPoints().size()));
        routeItemView.tvRouteVehicleNumber.setText(routeModel.getVehicleNumber());
        routeItemView.tvOwnerName.setText(routeModel.getRegisteredByVendorName());
        routeItemView.tvRouteName.setText(routeModel.getRouteName());
    }

    @Override
    public int getItemCount() {
        return routeModels.size();
    }

    class RouteItemView extends RecyclerView.ViewHolder {

        private TextView tvRouteName, tvRouteSource, tvRouteDestination, tvRouteWaypoints, tvRouteVehicleNumber, tvOwnerName;

        public RouteItemView(@NonNull View itemView) {
            super(itemView);

            tvRouteName = itemView.findViewById(R.id.tv_route_name);
            tvRouteSource = itemView.findViewById(R.id.tv_route_source);
            tvRouteDestination = itemView.findViewById(R.id.tv_route_destination);
            tvRouteWaypoints = itemView.findViewById(R.id.tv_route_assigned_waypoints);
            tvRouteVehicleNumber = itemView.findViewById(R.id.tv_route_assigned_vehicle);
            tvOwnerName = itemView.findViewById(R.id.tv_route_by_vendor);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onRouteSelectedListener != null)
                        onRouteSelectedListener.onRouteSelected(getAdapterPosition(), routeModels.get(getAdapterPosition()));
                }
            });
        }
    }
}
