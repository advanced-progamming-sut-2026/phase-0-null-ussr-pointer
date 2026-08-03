package com.ussr.pvz.view.loading;

import com.ussr.pvz.model.MenuState;

public final class LoadingCenter {
    private static MenuState requestedTarget;

    private LoadingCenter() {
    }

    public static void requestFor(MenuState target) {
        requestedTarget = target;
    }

    public static boolean consumeFor(MenuState target) {
        boolean requested = requestedTarget == target;
        requestedTarget = null;
        return requested;
    }
}