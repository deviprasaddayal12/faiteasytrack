package com.faiteasytrack.listeners;

import com.faiteasytrack.models.DashboardModel;

public interface OnStatisticsFetchListener {
    void onFetchingStarted(DashboardModel dashboardModel);

    void onFetchComplete(DashboardModel dashboardModel);
}
