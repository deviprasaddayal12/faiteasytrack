package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.models.VehicleModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VehicleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnVehicleSelectedListener {
        void onVehicleSelected(int position, VehicleModel vehicleModel);
    }

    private Context gContext;
    private ArrayList<VehicleModel> vehicleModels;
    private OnVehicleSelectedListener onVehicleSelectedListener;

    public VehicleAdapter(Context gContext, ArrayList<VehicleModel> vehicleModels, OnVehicleSelectedListener onVehicleSelectedListener) {
        this.gContext = gContext;
        this.vehicleModels = vehicleModels;
        this.onVehicleSelectedListener = onVehicleSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VehicleItemView(LayoutInflater.from(gContext).inflate(R.layout.row_vehicle_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VehicleModel vehicleModel = vehicleModels.get(position);

        VehicleItemView vehicleItemView = (VehicleItemView) holder;
        vehicleItemView.tvVehicleNumber.setText(vehicleModel.getVehicleNumber());
        vehicleItemView.tvAssignedDriver.setText(vehicleModel.getDriverName());
        vehicleItemView.tvOwningVendor.setText(vehicleModel.getRegisteredByVendorName());
        vehicleItemView.tvAssignedRoute.setText(vehicleModel.getRouteName());
    }

    @Override
    public int getItemCount() {
        return vehicleModels.size();
    }

    class VehicleItemView extends RecyclerView.ViewHolder {

        private TextView tvVehicleNumber, tvAssignedDriver, tvOwningVendor, tvAssignedRoute;

        public VehicleItemView(@NonNull View itemView) {
            super(itemView);

            tvVehicleNumber = itemView.findViewById(R.id.tv_vehicle_number);
            tvAssignedDriver = itemView.findViewById(R.id.tv_vehicle_assigned_driver);
            tvOwningVendor = itemView.findViewById(R.id.tv_vehicle_owning_vendor);
            tvAssignedRoute = itemView.findViewById(R.id.tv_vehicle_assigned_route);
            
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onVehicleSelectedListener != null)
                        onVehicleSelectedListener.onVehicleSelected(getAdapterPosition(), vehicleModels.get(getAdapterPosition()));
                }
            });
        }
    }
}
