package com.faiteasytrack.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faiteasytrack.R;
import com.faiteasytrack.constants.Dashboard;
import com.faiteasytrack.models.DashboardModel;
import com.faiteasytrack.utils.ViewUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnDashboardSelectedListener {
        void onDashboardItemSelected(int position, DashboardModel dashboardModel);
    }

    private Context context;
    private ArrayList<DashboardModel> dashboardModels;
    private OnDashboardSelectedListener onDashboardSelectedListener;

    public DashboardAdapter(Context context, ArrayList<DashboardModel> dashboardModels, OnDashboardSelectedListener onDashboardSelectedListener) {
        this.context = context;
        this.dashboardModels = dashboardModels;
        this.onDashboardSelectedListener = onDashboardSelectedListener;
    }

    @Override
    public int getItemCount() {
        return dashboardModels.size();
    }

    @Override
    public int getItemViewType(int position) {
        return dashboardModels.get(position).getStatisticsInfoType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case Dashboard.StatisticsType.TYPE_CHILDREN_FOR_PARENT: {
                return new ChildrenStatisticsView(LayoutInflater.from(context)
                        .inflate(R.layout.row_stats_children_for_parent, parent, false));
            }
            case Dashboard.StatisticsType.TYPE_DRIVER_FOR_VENDOR: {

            }
            case Dashboard.StatisticsType.TYPE_ROUTE_FOR_VENDOR: {

            }
            case Dashboard.StatisticsType.TYPE_TRIP_FOR_ANY_USER: {

            }
            case Dashboard.StatisticsType.TYPE_TRIP_FOR_DRIVER: {

            }
            case Dashboard.StatisticsType.TYPE_VEHICLE_FOR_VENDOR: {

            }
            default: {
                return new EmptyView(LayoutInflater.from(context)
                        .inflate(R.layout.layout_loader_statistics, parent, false));
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DashboardModel dashboardModel = dashboardModels.get(position);

        switch (dashboardModel.getStatisticsInfoType()) {
            case Dashboard.StatisticsType.TYPE_DEFAULT: {

            }
            break;
            case Dashboard.StatisticsType.TYPE_CHILDREN_FOR_PARENT: {
                ChildrenStatisticsView childrenStatisticsView = (ChildrenStatisticsView) holder;
                if (dashboardModel.isContainsData()){
                    ViewUtils.hideViews(childrenStatisticsView.llNoStatsLayout);
                    ViewUtils.showViews(childrenStatisticsView.llStatsAction);
                } else {
                    ViewUtils.showViews(childrenStatisticsView.llNoStatsLayout);
                    ViewUtils.hideViews(childrenStatisticsView.llStatsAction);
                }
            }
            break;
            case Dashboard.StatisticsType.TYPE_DRIVER_FOR_VENDOR: {

            }
            break;
            case Dashboard.StatisticsType.TYPE_ROUTE_FOR_VENDOR: {

            }
            break;
            case Dashboard.StatisticsType.TYPE_TRIP_FOR_ANY_USER: {

            }
            break;
            case Dashboard.StatisticsType.TYPE_TRIP_FOR_DRIVER: {

            }
            break;
            case Dashboard.StatisticsType.TYPE_VEHICLE_FOR_VENDOR: {

            }
            break;
        }
    }

    class EmptyView extends RecyclerView.ViewHolder {
        public EmptyView(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ChildrenStatisticsView extends RecyclerView.ViewHolder {

        private LinearLayout llNoStatsLayout, llStatsAction;
        private TextView tvNoStatsHeader, tvNoStatsSubHeader;
        private MaterialButton btnStatsAction;

        public ChildrenStatisticsView(@NonNull View itemView) {
            super(itemView);
            llNoStatsLayout = itemView.findViewById(R.id.ll_no_stats_layout);
            llStatsAction = itemView.findViewById(R.id.ll_no_stats_action);

            tvNoStatsHeader = itemView.findViewById(R.id.tv_no_stats_header);
            tvNoStatsSubHeader = itemView.findViewById(R.id.tv_no_stats_sub_header);
            btnStatsAction = itemView.findViewById(R.id.btn_no_stats_action);
        }
    }
}
