package com.ussr.pvz.model.dialogue;

import java.util.List;

public final class LevelOutroDialogueRegistry {
    private LevelOutroDialogueRegistry() {}

    public static List<String> getVictoryDialogue(
            String chapterId, String levelId) {

        return switch (chapterId) {
            case "ancient_egypt" -> victoryEgypt(levelId);
            case "frostbite_caves" -> victoryFrostbite(levelId);
            case "dark_ages" -> victoryDarkAges(levelId);
            case "big_wave_beach" -> victoryBeach(levelId);
            default -> List.of(
                    "Brainless beauties, every one of them!",
                    "Your plants are the real heroes today!"
            );
        };
    }

    public static List<String> getDefeatDialogue(
            String chapterId, String levelId) {

        return switch (chapterId) {
            case "ancient_egypt" -> defeatEgypt(levelId);
            case "frostbite_caves" -> defeatFrostbite(levelId);
            case "dark_ages" -> defeatDarkAges(levelId);
            case "big_wave_beach" -> defeatBeach(levelId);
            default -> List.of(
                    "Oh no, they got my brain!",
                    "Try again — you almost had them!"
            );
        };
    }

    // ── Ancient Egypt ────────────────────────────────────────────────────────

    private static List<String> victoryEgypt(String levelId) {
        return switch (levelId) {
            case "egypt_1_1" -> List.of(
                    "Mummy dearest, you lose!",
                    "Sand in their shoes AND defeat? Rough day!"
            );
            case "egypt_1_2" -> List.of(
                    "The pharaoh will NOT be pleased!",
                    "Your Sunflowers outshone the pyramids today!"
            );
            case "egypt_1_3" -> List.of(
                    "Even the Sphinx is impressed!",
                    "Three thousand years of history, and zombies still lose to plants!"
            );
            default -> List.of(
                    "By Ra's radiant beard, you did it!",
                    "The sands of Egypt are safe... for now!"
            );
        };
    }

    private static List<String> defeatEgypt(String levelId) {
        return switch (levelId) {
            case "egypt_1_1" -> List.of(
                    "The mummies got through!",
                    "Don't worry, even Egypt fell once or twice!"
            );
            case "egypt_1_2" -> List.of(
                    "That zombie had a lot of wrappings AND determination!",
                    "Plant more Sunflowers next time!"
            );
            case "egypt_1_3" -> List.of(
                    "Conquered by zombies... in Egypt. That's a new one.",
                    "The desert waits. Try again!"
            );
            default -> List.of(
                    "Oh no, the sands swallowed your defense!",
                    "Back to the drawing board — or the planting board!"
            );
        };
    }

    // ── Frostbite Caves ──────────────────────────────────────────────────────

    private static List<String> victoryFrostbite(String levelId) {
        return switch (levelId) {
            case "frost_2_1" -> List.of(
                    "Cool as a cucumber — literally!",
                    "Those zombies are on ice!"
            );
            case "frost_2_2" -> List.of(
                    "Brain-freeze for the zombies, victory for us!",
                    "Your Snow Peas were absolutely chilling!"
            );
            case "frost_2_3" -> List.of(
                    "Frozen solid! They will not be thawing anytime soon.",
                    "You turned the caves into a zombie freezer!"
            );
            default -> List.of(
                    "The cave is safe! My taco is still cold though.",
                    "Ice to meet you, victory!"
            );
        };
    }

    private static List<String> defeatFrostbite(String levelId) {
        return switch (levelId) {
            case "frost_2_1" -> List.of(
                    "The cold got to your plants before the zombies did!",
                    "Warm them up and try again!"
            );
            case "frost_2_2" -> List.of(
                    "Slipped on the ice AND lost? Double trouble!",
                    "Next time, watch your footing... and the zombies."
            );
            case "frost_2_3" -> List.of(
                    "The frozen horde was too much!",
                    "A warmer strategy might thaw the situation."
            );
            default -> List.of(
                    "Brrr... that did not go well.",
                    "The caves are relentless. You can be too!"
            );
        };
    }

    // ── Dark Ages ────────────────────────────────────────────────────────────

    private static List<String> victoryDarkAges(String levelId) {
        return switch (levelId) {
            case "dark_3_1" -> List.of(
                    "Hark! The zombies are vanquished!",
                    "A valiant effort, noble gardener!"
            );
            case "dark_3_2" -> List.of(
                    "The Black Knight of Botany strikes again!",
                    "Even the castle could not hold them — your plants could!"
            );
            case "dark_3_3" -> List.of(
                    "The dark is defeated by the green!",
                    "By the power of photosynthesis, you have won!"
            );
            default -> List.of(
                    "Forsooth! Victory is yours, brave planter!",
                    "The medieval menace is no more!"
            );
        };
    }

    private static List<String> defeatDarkAges(String levelId) {
        return switch (levelId) {
            case "dark_3_1" -> List.of(
                    "The zombies breached the castle walls!",
                    "Even knights lose sometimes. Charge again!"
            );
            case "dark_3_2" -> List.of(
                    "Alas, the dark won this round!",
                    "Perhaps a different formation of plants next time?"
            );
            case "dark_3_3" -> List.of(
                    "The graveyard grows tonight...",
                    "The ages are dark indeed. Light them with more Sunflowers!"
            );
            default -> List.of(
                    "Oh woe! The zombie hordes have prevailed!",
                    "Return to your garden and prepare anew!"
            );
        };
    }

    // ── Big Wave Beach ───────────────────────────────────────────────────────

    private static List<String> victoryBeach(String levelId) {
        return switch (levelId) {
            case "beach_4_1" -> List.of(
                    "Surf's up, zombies down!",
                    "You rode that wave all the way to victory!"
            );
            case "beach_4_2" -> List.of(
                    "Shell yeah! You did it!",
                    "The tide turned in your favour!"
            );
            case "beach_4_3" -> List.of(
                    "Beach, please — those zombies never had a chance!",
                    "Your aquatic plants made a splash today!"
            );
            default -> List.of(
                    "Wipeout for the zombies!",
                    "The beach is clean — well, cleaner."
            );
        };
    }

    private static List<String> defeatBeach(String levelId) {
        return switch (levelId) {
            case "beach_4_1" -> List.of(
                    "The wave washed your defense away!",
                    "Sea you on the retry screen!"
            );
            case "beach_4_2" -> List.of(
                    "They came from the deep — and won!",
                    "Reinforce the shoreline and try again."
            );
            case "beach_4_3" -> List.of(
                    "Even the lifeguard plants could not save you!",
                    "The ocean is patient. So can you be!"
            );
            default -> List.of(
                    "Wiped out! The zombie surfers claimed the beach.",
                    "Dry off and come back stronger!"
            );
        };
    }
}