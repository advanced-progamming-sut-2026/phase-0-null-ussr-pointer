package com.ussr.pvz.service;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.dto.GreenhousePotRequest;
import com.ussr.pvz.model.greenhouse.SproutPlant;

public class GreenHouseService {

    private int[] parseCoordinates(GreenhousePotRequest request) {
        try {
            int x = Integer.parseInt(request.x());
            int y = Integer.parseInt(request.y());
            return new int[]{x, y};
        } catch (NumberFormatException e) {
            throw new NumberFormatException("number format is wrong");
        }
    }

    private void validatePotUnlocked(int x, int y) {
        if (!App.getAccount().getGreenhouse().isPotUnlocked(x, y)) {
            throw new IllegalStateException("Pot is locked");
        }
    }

    private void validatePotOccupied(int x, int y) {
        if (!App.getAccount().getGreenhouse().isPotOccupied(x, y)) {
            throw new IllegalStateException("Pot is currently not in use");
        }
    }

    private void validatePotNotOccupied(int x, int y) {
        if (App.getAccount().getGreenhouse().isPotOccupied(x, y)) {
            throw new IllegalStateException("Pot is currently in use");
        }
    }

    public String plant(GreenhousePotRequest request) {
        int[] coords = parseCoordinates(request);
        int x = coords[0];
        int y = coords[1];

        validatePotUnlocked(x, y);
        validatePotNotOccupied(x, y);

        App.getAccount().getGreenhouse().plant(x, y, App.getAccount().getCollection());
        return "Plant planted in " + x + " " + y + "successfully";
    }

    public String collect(GreenhousePotRequest request) {
        int[] coords = parseCoordinates(request);
        int x = coords[0];
        int y = coords[1];

        validatePotUnlocked(x, y);
        validatePotOccupied(x, y);

        SproutPlant result = null;
        try {
            result = App.getAccount().getGreenhouse().collect(x, y);
        } catch (Exception e) {
            return e.getMessage();
        }

        if (result.isReady()) {
            App.getAccount().getAdventureProgress().addCoin(500);

            return "The plant collected successfully and 500 coin added to your wallet";
        } else {
            //todo implement the a reserved boost for that plant and change the return message
            return "The plant collected successfully and a boost is there for you";
        }
    }

    public String grow(GreenhousePotRequest request) {
        int[] coords = parseCoordinates(request);
        int x = coords[0];
        int y = coords[1];

        validatePotUnlocked(x, y);
        validatePotOccupied(x, y);

        try {
            int cost = App.getAccount().getGreenhouse().speedUp(x, y);
            int currentGem = App.getAccount().getAdventureProgress().getGem();
            if (cost > currentGem ) {
                throw new IllegalStateException("You don't have enough money");
            }else{
                App.getAccount().getGreenhouse().grow(x, y);
                App.getAccount().getAdventureProgress().addGem(-cost);
                return "The plant grew successfully and is ready to collect";
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    public String showGreenHouse() {
        java.util.Map<String, Object> data = App.getAccount().getGreenhouse().toMap();
        java.util.List<java.util.Map<String, Object>> potsList =
                (java.util.List<java.util.Map<String, Object>>) data.get("pots");

        int maxRows = com.ussr.pvz.model.greenhouse.Greenhouse.MAX_ROWS;
        int maxCols = com.ussr.pvz.model.greenhouse.Greenhouse.MAX_COLS;
        java.util.Map<String, Object>[][] grid = new java.util.Map[maxRows][maxCols];

        if (potsList != null) {
            for (java.util.Map<String, Object> potMap : potsList) {
                int x = ((Number) potMap.get("x")).intValue(); // Column coordinate
                int y = ((Number) potMap.get("y")).intValue(); // Row coordinate
                if (y >= 0 && y < maxRows && x >= 0 && x < maxCols) {
                    grid[y][x] = potMap;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < maxRows; y++) {
            for (int x = 0; x < maxCols; x++) {
                java.util.Map<String, Object> pot = grid[y][x];

                if (pot == null) {
                    sb.append("[LOCKED]");
                } else {
                    boolean unlocked = (boolean) pot.get("unlocked");
                    boolean occupied = (boolean) pot.get("occupied");

                    if (!unlocked) {
                        sb.append("[LOCKED]");
                    } else if (!occupied || !pot.containsKey("plant")) {
                        sb.append("[EMPTY]");
                    } else {
                        java.util.Map<String, Object> plant = (java.util.Map<String, Object>) pot.get("plant");
                        String stateStr = (String) plant.get("state");

                        if ("READY".equals(stateStr)) {
                            sb.append("[READY]");
                        } else {
                            String name = (String) plant.get("name");
                            if (name == null || name.isEmpty()) {
                                name = "Marigold";
                            }

                            long targetTime = ((Number) plant.get("growTargetTime")).longValue();
                            long remainingMillis = targetTime - System.currentTimeMillis();
                            long remainingHours = Math.max(0, (remainingMillis + (60 * 60 * 1000) - 1) / (60 * 60 * 1000));

                            sb.append(String.format("[%s: %dh remaining]", name, remainingHours));
                        }
                    }
                }

                if (x < maxCols - 1) {
                    sb.append(" | ");
                }
            }
            if (y < maxRows - 1) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }
}