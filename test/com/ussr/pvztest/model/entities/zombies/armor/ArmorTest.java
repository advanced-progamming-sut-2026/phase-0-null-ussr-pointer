package com.ussr.pvztest.model.entities.zombies.armor;

import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.armor.ArmorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArmorTest {

    private Armor coneArmor;

    @BeforeEach
    void setUp() {
        // Cone armor has 370 base HP based on ArmorType data
        coneArmor = new Armor(ArmorType.CONE, 370);
    }

    @Test
    @DisplayName("✅ Should absorb damage and return 0 leftover when armor holds")
    void takeDamage_shouldAbsorbFully() {
        int leftover = coneArmor.takeDamage(100);

        assertEquals(0, leftover);
        assertEquals(270, coneArmor.getArmorHp());
        assertFalse(coneArmor.isDestroyed());
    }

    @Test
    @DisplayName("✅ Should return leftover damage when armor is destroyed")
    void takeDamage_shouldReturnLeftover_whenOverkilled() {
        int leftover = coneArmor.takeDamage(400); // 370 HP - 400 DMG = -30

        assertEquals(30, leftover);
        assertEquals(0, coneArmor.getArmorHp());
        assertTrue(coneArmor.isDestroyed());
    }

    @Test
    @DisplayName("✅ Should correctly calculate damage layers for visual state transitions")
    void getDamageLayer_shouldCalculateThresholdsCorrectly() {
        // Layer 0: > 66.6% ( > 246 HP)
        assertEquals(0, coneArmor.getDamageLayer());

        // Layer 1: 33.3% to 66.6% (123 to 246 HP)
        coneArmor.takeDamage(150); // HP now 220
        assertEquals(1, coneArmor.getDamageLayer());

        // Layer 2: < 33.3% (< 123 HP)
        coneArmor.takeDamage(120); // HP now 100
        assertEquals(2, coneArmor.getDamageLayer());
    }
}