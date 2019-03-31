package com.faiteasytrack.observables;

import java.util.Observable;

public class ObservableBoolean {

    public static final String TAG = ObservableBoolean.class.getSimpleName();

    public static class Map extends Observable{
        private boolean isReady;

        public boolean isReady() {
            return isReady;
        }

        public void setReady(boolean ready) {
            this.isReady = ready;
            notifyObservers(ready);
        }
    }

    public static class Service extends Observable{
        private boolean isBound;

        public boolean isBound() {
            return isBound;
        }

        public void setBound(boolean bound) {
            this.isBound = bound;
            notifyObservers(bound);
        }
    }

    public static class Location extends Observable{
        private boolean isReceived;

        public boolean isReceived() {
            return isReceived;
        }

        public void setReceived(boolean received) {
            this.isReceived = received;
            notifyObservers(received);
        }
    }

    public static class Gps extends Observable{
        private boolean isOn;

        public boolean isOn() {
            return isOn;
        }

        public void setOn(boolean on) {
            this.isOn = on;
            notifyObservers(on);
        }
    }

    public static class Permission extends Observable{
        private boolean isGranted;

        public boolean isGranted() {
            return isGranted;
        }

        public void setGranted(boolean granted) {
            this.isGranted = granted;
            notifyObservers(granted);
        }
    }

    public static class Internet extends Observable{
        private boolean isOnline;

        public boolean isOnline() {
            return isOnline;
        }

        public void setOnline(boolean online) {
            this.isOnline = online;
            notifyObservers(online);
        }
    }
}
