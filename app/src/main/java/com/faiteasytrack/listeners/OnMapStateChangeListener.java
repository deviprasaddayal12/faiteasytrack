package com.faiteasytrack.listeners;

public interface OnMapStateChangeListener {
    int MODE_IDEAL = 0;
    int MODE_TRACING = 1;
    int MODE_TRACKING = 2;

    void onIdealStateLost(int mode);

    void onIdealStateRestored(int mode);
}
