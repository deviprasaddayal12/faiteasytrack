package com.faiteasytrack.managers;

import android.content.Context;

import com.faiteasytrack.constants.Dashboard;
import com.faiteasytrack.listeners.OnStatisticsFetchListener;
import com.faiteasytrack.models.DashboardModel;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

public class DashboardManager {

    public static final String TAG = "DashboardManager";

    private static DashboardManager dashboardManager = null;

    private Context context;
    private OnStatisticsFetchListener statisticsFetchListener;

    public static DashboardManager getInstance(Context context){
        if (dashboardManager == null)
            dashboardManager = new DashboardManager(context);

        return dashboardManager;
    }

    private DashboardManager(Context context) {
        this.context = context;
    }

    public void getStatisticsReport(){
        DashboardModel dashboardModelDefault = new DashboardModel();
        dashboardModelDefault.setStatisticsInfoType(Dashboard.StatisticsType.TYPE_DEFAULT);

        if (statisticsFetchListener != null)
            statisticsFetchListener.onFetchingStarted(dashboardModelDefault);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                DashboardModel dashboardModel = new DashboardModel();
                dashboardModel.setStatisticsInfoType(Dashboard.StatisticsType.TYPE_CHILDREN_FOR_PARENT);
                dashboardModel.setContainsData(false);

                if (statisticsFetchListener != null)
                    statisticsFetchListener.onFetchComplete(dashboardModel);
            }
        }, 5000);
    }

    public OnStatisticsFetchListener getStatisticsFetchListener() {
        return statisticsFetchListener;
    }

    public void setStatisticsFetchListener(OnStatisticsFetchListener statisticsFetchListener) {
        this.statisticsFetchListener = statisticsFetchListener;
    }
}
