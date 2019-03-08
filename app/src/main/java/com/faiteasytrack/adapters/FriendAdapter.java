package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.models.FriendModel;
import com.faiteasytrack.utils.Utils;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnFriendSelectedListener {
        void onFriendSelected(int position, FriendModel friendModel);
    }

    private Context gContext;
    private ArrayList<FriendModel> friendModels;
    private OnFriendSelectedListener friendSelectedListener;

    public FriendAdapter(Context gContext, ArrayList<FriendModel> friendModels, OnFriendSelectedListener friendSelectedListener) {
        this.gContext = gContext;
        this.friendModels = friendModels;
        this.friendSelectedListener = friendSelectedListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendItemView(LayoutInflater.from(gContext).inflate(R.layout.row_friend_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FriendModel friendModel = friendModels.get(position);

        FriendItemView friendItemView = (FriendItemView) holder;
        friendItemView.tvFriendName.setText(friendModel.getName());
        friendItemView.tvFriendsAt.setText(Utils.getTimeInString(friendModel.getFriendsAtMillis()));
    }

    @Override
    public int getItemCount() {
        return friendModels.size();
    }

    class FriendItemView extends RecyclerView.ViewHolder {

        private TextView tvFriendName, tvFriendsAt;

        public FriendItemView(@NonNull View itemView) {
            super(itemView);

            tvFriendsAt = itemView.findViewById(R.id.tv_friend_millis);
            tvFriendName = itemView.findViewById(R.id.tv_friend_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (friendSelectedListener != null)
                        friendSelectedListener.onFriendSelected(getAdapterPosition(), friendModels.get(getAdapterPosition()));
                }
            });
        }
    }
}
