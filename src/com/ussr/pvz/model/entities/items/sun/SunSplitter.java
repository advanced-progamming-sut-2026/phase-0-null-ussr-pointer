package com.ussr.pvz.model.entities.items.sun;

import java.util.ArrayList;
import java.util.List;

public final class SunSplitter {

    private SunSplitter() {
    }

    public static List<Integer> split(int total) {
        List<Integer> result = new ArrayList<>();
        int remaining = total;

        while (remaining >= 75) {
            result.add(75);
            remaining -= 75;
        }
        if (remaining >= 50) {
            result.add(50);
            remaining -= 50;
        }
        if (remaining >= 25) {
            result.add(25);
            remaining -= 25;
        }
        if (remaining > 0) {
            // leftover from a non-multiple-of-25 total (e.g. upgrade buffs) - top it up
            result.add(25);
        }
        if (result.isEmpty() && total > 0) {
            result.add(total);
        }
        return result;
    }
}