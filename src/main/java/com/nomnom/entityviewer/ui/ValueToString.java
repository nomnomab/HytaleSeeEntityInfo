package com.nomnom.entityviewer.ui;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;

import java.text.NumberFormat;

public class ValueToString {
    private static final NumberFormat _numberFormat = NumberFormat.getInstance();

    public ValueToString() {
        _numberFormat.setMaximumFractionDigits(2);
    }

    public static String Vector3d(Vector3d vector) {
        return _numberFormat.format(vector.x) + ", " +
                _numberFormat.format(vector.y) + ", " +
                _numberFormat.format(vector.z);
    }

    public static String Vector3f(Vector3f vector) {
        return _numberFormat.format(vector.x) + ", " +
                _numberFormat.format(vector.y) + ", " +
                _numberFormat.format(vector.z);
    }
}
