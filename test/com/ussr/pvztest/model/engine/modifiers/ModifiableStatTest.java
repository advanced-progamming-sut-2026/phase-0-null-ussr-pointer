package com.ussr.pvztest.model.engine.modifiers;

import com.ussr.pvz.model.engine.modifiers.ModifiableStat;
import com.ussr.pvz.model.engine.modifiers.ModifierType;
import com.ussr.pvz.model.engine.modifiers.StatModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModifiableStatTest {

    private ModifiableStat stat;

    @BeforeEach
    void setUp() {
        stat = new ModifiableStat(100.0f); // Base value 100
    }

    @Test
    @DisplayName("✅ Should apply flat and percentage modifiers correctly")
    void getValue_shouldApplyModifiersCorrectly() {
        // Arrange
        // Note: The logic in ModifiableStat applies modifiers sequentially.
        // It applies flat modifiers first (mod.apply(0) > 0 && mod.apply(1) == mod.apply(0) + 1 implies FLAT)
        stat.addModifier(new StatModifier("flat_buff", ModifierType.FLAT, 50.0f));
        stat.addModifier(new StatModifier("perc_buff", ModifierType.PERCENTAGE, 0.5f)); // +50%

        // Act
        float finalValue = stat.getValue();

        // Assert
        // Base: 100 + Flat: 50 = 150
        // 150 + 50% = 225
        assertEquals(225.0f, finalValue, 0.01f);
    }

    @Test
    @DisplayName("✅ Should remove expired temporary modifiers upon update")
    void update_shouldPurgeExpiredModifiers() {
        // Arrange
        stat.addModifier(new StatModifier("temp_buff", ModifierType.FLAT, 20.0f, 2.0f)); // 2s duration
        assertEquals(120.0f, stat.getValue(), 0.01f);

        // Act - Simulate 2.5 seconds passing
        stat.update(1.0f);
        stat.update(1.5f);

        // Assert
        assertEquals(100.0f, stat.getValue(), 0.01f); // Back to base
    }

    @Test
    @DisplayName("✅ Should override existing modifier with the same ID")
    void addModifier_shouldOverrideSameId() {
        // Arrange
        stat.addModifier(new StatModifier("unique_buff", ModifierType.FLAT, 20.0f));
        stat.addModifier(new StatModifier("unique_buff", ModifierType.FLAT, 50.0f));

        // Act
        float finalValue = stat.getValue();

        // Assert
        assertEquals(150.0f, finalValue, 0.01f); // Only the 50.0f buff applies
    }
}