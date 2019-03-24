package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.constants.Request;
import com.faiteasytrack.models.ContactModel;
import com.faiteasytrack.models.RequestStatusModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnContactSelectedListener {
        void onContactSelected(int position, ContactModel contactModel);
    }

    private Context gContext;
    private ArrayList<ContactModel> contactModels;
    private OnContactSelectedListener contactSelectedListener;

    public ContactsAdapter(Context gContext, ArrayList<ContactModel> contactModels, OnContactSelectedListener contactSelectedListener) {
        this.gContext = gContext;
        this.contactModels = contactModels;
        this.contactSelectedListener = contactSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactsItemView(LayoutInflater.from(gContext).inflate(R.layout.row_contact_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContactModel contactModel = contactModels.get(position);

        ContactsItemView contactsItemView = (ContactsItemView) holder;
        contactsItemView.tvContactName.setText(contactModel.getContactName());
        if (contactModel.getPhones().size() > 0)
            contactsItemView.tvPhone.setText(contactModel.getPhones().get(0));

        RequestStatusModel requestStatusModel = contactModel.getRequestStatusModel();
        if (requestStatusModel != null) {
            if (requestStatusModel.getStatus() == Request.REQUEST_NOT_YET
                    || requestStatusModel.getStatus() == Request.REQUEST_SEND_FAILED) {
//                        contactsItemView.tvRequestStatus.setText(requestStatusModel.getTitle());
//                        contactsItemView.tvRequestStatus.setVisibility(View.GONE);
                contactsItemView.btnRequest.setText(requestStatusModel.getTitle());
                contactsItemView.btnRequest.setVisibility(View.VISIBLE);
            } else {
//                        contactsItemView.tvRequestStatus.setText(requestStatusModel.getTitle());
//                        contactsItemView.tvRequestStatus.setVisibility(View.VISIBLE);
                contactsItemView.btnRequest.setText(requestStatusModel.getTitle());
                contactsItemView.btnRequest.setVisibility(View.GONE);
            }
        } else {
//                    contactsItemView.tvRequestStatus.setVisibility(View.GONE);
            contactsItemView.btnRequest.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return contactModels.size();
    }

    class ContactsItemView extends RecyclerView.ViewHolder {

        private TextView tvContactName, tvPhone, tvRequestStatus;
        private Button btnRequest;

        public ContactsItemView(@NonNull View itemView) {
            super(itemView);

            tvPhone = itemView.findViewById(R.id.tv_contact_phone);
            tvContactName = itemView.findViewById(R.id.tv_contact_name);

            tvRequestStatus = itemView.findViewById(R.id.tv_status);

            btnRequest = itemView.findViewById(R.id.btn_status);
            btnRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (contactSelectedListener != null)
                        contactSelectedListener.onContactSelected(getAdapterPosition(), contactModels.get(getAdapterPosition()));
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
