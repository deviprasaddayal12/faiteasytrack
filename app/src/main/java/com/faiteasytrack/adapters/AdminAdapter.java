package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.models.AdminModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AdminAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnAdminSelectedListener {
        void onAdminSelected(int position, AdminModel adminModel);
    }

    private Context gContext;
    private ArrayList<AdminModel> adminModels;
    private OnAdminSelectedListener onAdminSelectedListener;

    public AdminAdapter(Context gContext, ArrayList<AdminModel> adminModels, OnAdminSelectedListener onAdminSelectedListener) {
        this.gContext = gContext;
        this.adminModels = adminModels;
        this.onAdminSelectedListener = onAdminSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AdminItemView(LayoutInflater.from(gContext).inflate(R.layout.row_admin_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AdminModel adminModel = adminModels.get(position);

        AdminItemView adminItemView = (AdminItemView) holder;
        adminItemView.tvAdminName.setText(adminModel.getName());
        adminItemView.tvAdminPhone.setText(adminModel.getPhoneNumber());
    }

    @Override
    public int getItemCount() {
        return adminModels.size();
    }

    class AdminItemView extends RecyclerView.ViewHolder {

        private TextView tvAdminName, tvAdminPhone;

        public AdminItemView(@NonNull View itemView) {
            super(itemView);

            tvAdminName = itemView.findViewById(R.id.tv_admin_name);
            tvAdminPhone = itemView.findViewById(R.id.tv_admin_phone);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onAdminSelectedListener != null)
                        onAdminSelectedListener.onAdminSelected(getAdapterPosition(), adminModels.get(getAdapterPosition()));
                }
            });
        }
    }
}
