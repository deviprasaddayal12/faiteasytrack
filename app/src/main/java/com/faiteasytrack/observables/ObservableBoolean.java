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
            setChanged();
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
            setChanged();
            notifyObservers(bound);
        }
    }

    public static class Position extends Observable{
        private boolean isReceived;

        public boolean isReceived() {
            return isReceived;
        }

        public void setReceived(boolean received) {
            this.isReceived = received;
            setChanged();
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
            setChanged();
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
            setChanged();
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
            setChanged();
            notifyObservers(online);
        }
    }

    public static class Tracking extends Observable{
        private boolean isTracking;

        public Tracking(boolean isTracking) {
            this.isTracking = isTracking;
        }

        public boolean isTracking() {
            return isTracking;
        }

        public void setTracking(boolean tracking) {
            this.isTracking = tracking;
            setChanged();
            notifyObservers(tracking);
        }
    }

    public static class Tracing extends Observable{
        private boolean isTracing;

        public Tracing(boolean isTracing) {
            this.isTracing = isTracing;
        }

        public boolean isTracing() {
            return isTracing;
        }

        public void setTracing(boolean tracing) {
            this.isTracing = tracing;
            setChanged();
            notifyObservers(tracing);
        }
    }
}
