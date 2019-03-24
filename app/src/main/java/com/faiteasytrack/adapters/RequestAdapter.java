package com.faiteasytrack.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.constants.Request;
import com.faiteasytrack.models.RequestModel;
import com.faiteasytrack.utils.Utils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnRequestSelectedListener {
        void onRequestSelected(int position, RequestModel requestModel);

        void onRequestUpdated(int position, RequestModel requestModel, boolean isAccepted);
    }

    private Context gContext;
    private ArrayList<RequestModel> requestModels;
    private OnRequestSelectedListener requestSelectedListener;

    public RequestAdapter(Context gContext, ArrayList<RequestModel> requestModels, OnRequestSelectedListener requestSelectedListener) {
        this.gContext = gContext;
        this.requestModels = requestModels;
        this.requestSelectedListener = requestSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RequestItemView(LayoutInflater.from(gContext).inflate(R.layout.row_request_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RequestModel requestModel = requestModels.get(position);

        RequestItemView itemView = (RequestItemView) holder;
        itemView.tvRequesteeName.setText(requestModel.getRequesteeName());
        itemView.tvRequestTime.setText(Utils.getTimeInString(requestModel.getRequestedAtMillis()));

        if (requestModel.getRequestStatusModel().getStatus() != Request.REQUEST_SENT) {
            itemView.llRequestAction.setVisibility(View.GONE);
        } else {
            if (requestModel.getRequestStatusModel().getStatus() == Request.REQUEST_ACCEPTED) {
                itemView.tvStatus.setTextColor(Color.GREEN);
                itemView.tvStatus.setText("Cheers! You are friends now!");
            } else {
                itemView.tvStatus.setTextColor(Color.RED);
                itemView.tvStatus.setText("Sorry! You are denied!");
            }
        }
    }

    @Override
    public int getItemCount() {
        return requestModels.size();
    }

    class RequestItemView extends RecyclerView.ViewHolder {

        private TextView tvRequesteeName, tvRequestTime, tvStatus;
        private Button btnReject, btnAccept;
        private LinearLayout llRequestAction;

        public RequestItemView(@NonNull View itemView) {
            super(itemView);

            tvRequestTime = itemView.findViewById(R.id.tv_requestee_millis);
            tvRequesteeName = itemView.findViewById(R.id.tv_requestee_name);
            tvStatus = itemView.findViewById(R.id.tv_status);

            btnAccept = itemView.findViewById(R.id.btn_accept_request);
            btnReject = itemView.findViewById(R.id.btn_reject_request);

            llRequestAction = itemView.findViewById(R.id.ll_request_action);

            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (requestSelectedListener != null)
                        requestSelectedListener.onRequestUpdated(getAdapterPosition(), requestModels.get(getAdapterPosition()), true);
                }
            });
            btnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (requestSelectedListener != null)
                        requestSelectedListener.onRequestUpdated(getAdapterPosition(), requestModels.get(getAdapterPosition()), false);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (requestSelectedListener != null)
                        requestSelectedListener.onRequestSelected(getAdapterPosition(), requestModels.get(getAdapterPosition()));
                }
            });
        }
    }
}
