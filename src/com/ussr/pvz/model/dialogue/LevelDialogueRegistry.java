package com.ussr.pvz.model.dialogue;

import java.util.List;

public final class LevelDialogueRegistry {
    private LevelDialogueRegistry() {
    }

    public static List<String> getDialogue(
            String chapterId,
            String levelId
    ) {
        return switch (chapterId) {
            case "ancient_egypt" -> List.of(
                    "That is a lot of sand!",
                    "Watch out for the mummies!",
                    "Ready? Let's defend the lawn!"
            );

            case "frostbite_caves" -> List.of(
                    "My taco is frozen!",
                    "Be careful on the slippery ice!",
                    "Here come the zombies!"
            );

            case "dark_ages" -> List.of(
                    "It is really dark here.",
                    "Those graves look suspicious!",
                    "Get your plants ready!"
            );

            case "big_wave_beach" -> List.of(
                    "Water everywhere!",
                    "Use your aquatic plants carefully!",
                    "Incoming wave!"
            );

            default -> List.of(
                    "The zombies are coming!",
                    "Prepare your plants!"
            );
        };
    }
}