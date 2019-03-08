package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.models.VendorModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VendorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnVendorSelectedListener {
        void onVendorSelected(int position, VendorModel vendorModel);
    }

    private Context gContext;
    private ArrayList<VendorModel> vendorModels;
    private OnVendorSelectedListener onVendorSelectedListener;

    public VendorAdapter(Context gContext, ArrayList<VendorModel> vendorModels, OnVendorSelectedListener onVendorSelectedListener) {
        this.gContext = gContext;
        this.vendorModels = vendorModels;
        this.onVendorSelectedListener = onVendorSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VendorItemView(LayoutInflater.from(gContext).inflate(R.layout.row_vendor_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        VendorModel vendorModel = vendorModels.get(position);

        VendorItemView vendorItemView = (VendorItemView) holder;
        vendorItemView.tvVendorName.setText(vendorModel.getName());
        vendorItemView.tvVendorPhone.setText(vendorModel.getPhoneNumber());
        vendorItemView.tvVendorCode.setText(vendorModel.getCode());
        vendorItemView.tvVendorPassword.setText(vendorModel.getPassword());
    }

    @Override
    public int getItemCount() {
        return vendorModels.size();
    }

    class VendorItemView extends RecyclerView.ViewHolder {

        private TextView tvVendorName, tvVendorPhone, tvVendorCode, tvVendorPassword;

        public VendorItemView(@NonNull View itemView) {
            super(itemView);

            tvVendorName = itemView.findViewById(R.id.tv_vendor_name);
            tvVendorPhone = itemView.findViewById(R.id.tv_vendor_phone);
            tvVendorCode = itemView.findViewById(R.id.tv_vendor_code);
            tvVendorPassword = itemView.findViewById(R.id.tv_vendor_password);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onVendorSelectedListener != null)
                        onVendorSelectedListener.onVendorSelected(getAdapterPosition(), vendorModels.get(getAdapterPosition()));
                }
            });
        }
    }
}
