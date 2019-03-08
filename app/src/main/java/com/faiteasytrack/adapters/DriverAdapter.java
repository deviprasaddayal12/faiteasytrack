package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.models.DriverModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DriverAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnDriverSelectedListener {
        void onDriverSelected(int position, DriverModel driverModel);
    }

    private Context gContext;
    private ArrayList<DriverModel> driverModels;
    private OnDriverSelectedListener onDriverSelectedListener;

    public DriverAdapter(Context gContext, ArrayList<DriverModel> driverModels, OnDriverSelectedListener onDriverSelectedListener) {
        this.gContext = gContext;
        this.driverModels = driverModels;
        this.onDriverSelectedListener = onDriverSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DriverItemView(LayoutInflater.from(gContext).inflate(R.layout.row_driver_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DriverModel driverModel = driverModels.get(position);

        DriverItemView driverItemView = (DriverItemView) holder;
        driverItemView.tvDriverName.setText(driverModel.getName());
        driverItemView.tvDriverPhone.setText(driverModel.getPhoneNumber());
        driverItemView.tvDriverCode.setText(driverModel.getCode());
        driverItemView.tvDriverPassword.setText(driverModel.getPassword());
        driverItemView.tvDriverVehicle.setText(driverModel.getVehicleNumber());
        driverItemView.tvDriverRoute.setText(driverModel.getRouteName());
    }

    @Override
    public int getItemCount() {
        return driverModels.size();
    }

    class DriverItemView extends RecyclerView.ViewHolder {

        private TextView tvDriverName, tvDriverPhone, tvDriverCode, tvDriverPassword,
                tvDriverVehicle, tvDriverRoute;

        public DriverItemView(@NonNull View itemView) {
            super(itemView);

            tvDriverName = itemView.findViewById(R.id.tv_driver_name);
            tvDriverPhone = itemView.findViewById(R.id.tv_driver_phone);
            tvDriverCode = itemView.findViewById(R.id.tv_driver_code);
            tvDriverPassword = itemView.findViewById(R.id.tv_driver_password);
            tvDriverVehicle = itemView.findViewById(R.id.tv_driver_assigned_vehicle);
            tvDriverRoute = itemView.findViewById(R.id.tv_driver_assigned_route);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onDriverSelectedListener != null)
                        onDriverSelectedListener.onDriverSelected(getAdapterPosition(), driverModels.get(getAdapterPosition()));
                }
            });
        }
    }
}
