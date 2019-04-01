package com.faiteasytrack.observables;

import java.util.Observable;

public class ObservableInteger extends Observable {

    public static final String TAG = ObservableInteger.class.getSimpleName();

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
        notifyObservers(value);
    }
}
