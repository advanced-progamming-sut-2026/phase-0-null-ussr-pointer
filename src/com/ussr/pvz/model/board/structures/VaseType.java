package com.ussr.pvz.model.board.structures;

public enum VaseType {
    NORMAL("768/FULL/VASEBREAKER/VASE_BROWN/VASE_BROWN.PAM"),
    PLANT("768/FULL/VASEBREAKER/VASE_GREEN/VASE_GREEN.PAM"),
    GARGANTAUR("768/FULL/VASEBREAKER/VASE_GARGANTUAR/VASE_GARGANTUAR.PAM");

    private final String pamLocation;

    VaseType(String pamLocation) {
        this.pamLocation = pamLocation;
    }

    public String getPamLocation()
    {
        return pamLocation;
    }
}
