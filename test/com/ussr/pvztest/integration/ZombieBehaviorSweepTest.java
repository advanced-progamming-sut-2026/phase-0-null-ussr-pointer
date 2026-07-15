package com.ussr.pvztest.integration;

import com.ussr.pvz.model.App;
import com.ussr.pvz.model.account.Account;
import com.ussr.pvz.model.account.AccountState;
import com.ussr.pvz.model.account.Gender;
import com.ussr.pvz.model.entities.zombies.Zombie;
import com.ussr.pvz.model.entities.zombies.ZombieFactory;
import com.ussr.pvz.model.entities.zombies.armor.Armor;
import com.ussr.pvz.model.entities.zombies.armor.ArmorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Wide-coverage sweep over ZombieFactory-produced zombies: construction from
 * zombies.json blueprints, armor resolution, damage/armor interplay, and
 * cost lookups used by wave spawning. Runs many aliases and scenarios
 * through a shared harness rather than splitting into many tiny classes.
 */
class ZombieBehaviorSweepTest {

    @BeforeEach
    void setUp() {
        App.getAccounts().clear();
        AccountState state = new AccountState(
                "zombie-user", "ZTest", "pass", "z@example.com", Gender.MALE, 3,
                null, null, 1, 1, 0, 0, 0, 0, 0,
                new HashMap<>(), new ArrayList<>(), new ArrayList<>(),
                null, null, 0, new HashMap<>(), new ArrayList<>(), new HashMap<>()
        );
        Account account = new Account(state, null);
        App.addAccount(account);
        App.login(account);

        ZombieFactory.init();
    }

    // ====== CONSTRUCTION FROM BLUEPRINT ======

    @ParameterizedTest
    @DisplayName("✅ Known aliases construct successfully with positive HP and non-null behaviors")
    @ValueSource(strings = {
            "ZombieDefault", "ZombieArmor1", "ZombieArmor2", "ZombieImp",
            "ZombieGargantuar", "ZombieRa", "ZombieNewspaper", "ZombieArcade"
    })
    void create_knownAliases_produceValidZombie(String alias) {
        Zombie zombie = ZombieFactory.create(alias, 2, 5);

        assertTrue(zombie.getHp() > 0, alias + " should have positive HP");
        assertNotNull(zombie.getMoveBehavior(), alias + " should have a move behavior");
        assertNotNull(zombie.getAttackBehavior(), alias + " should have an attack behavior");
        assertNotNull(zombie.getDefenseBehavior(), alias + " should have a defense behavior");
        assertTrue(zombie.isAlive());
    }

    @Test
    @DisplayName("❌ Unknown alias throws IllegalArgumentException")
    void create_unknownAlias_throws() {
        assertThrows(IllegalArgumentException.class, () -> ZombieFactory.create("NotARealZombie", 0, 0));
    }

    @Test
    @DisplayName("✅ Spawn position matches requested row/col")
    void create_setsPositionFromRowAndCol() {
        Zombie zombie = ZombieFactory.create("ZombieDefault", 3, 7);

        assertEquals(7, zombie.getPosition().x(), 0.001);
        assertEquals(3, zombie.getPosition().y(), 0.001);
    }

    @Test
    @DisplayName("✅ Zombies move leftward (negative X speed) by default")
    void create_movesLeftward_bydefault() {
        Zombie zombie = ZombieFactory.create("ZombieDefault", 0, 0);
        assertTrue(zombie.getSpeed().x() < 0);
    }

    // ====== ARMOR RESOLUTION ======

    @ParameterizedTest
    @DisplayName("✅ Armored aliases get correctly typed, positive-HP armor")
    @CsvSource({
            "ZombieArmor1, CONE",
            "ZombieArmor2, BUCKET",
            "ZombieArmor4, BRICK"
    })
    void create_armoredAliases_resolveCorrectArmorType(String alias, String expectedType) {
        Zombie zombie = ZombieFactory.create(alias, 0, 0);

        assertNotNull(zombie.getArmor(), alias + " should have armor");
        assertEquals(ArmorType.valueOf(expectedType), zombie.getArmor().getArmorType());
        assertTrue(zombie.getArmor().getArmorHp() > 0);
    }

    @Test
    @DisplayName("✅ Unarmored alias has null armor")
    void create_unarmoredAlias_hasNullArmor() {
        Zombie zombie = ZombieFactory.create("ZombieDefault", 0, 0);
        assertNull(zombie.getArmor());
    }

    @Test
    @DisplayName("✅ Multi-piece armor (crown+shoulder) accumulates combined HP")
    void create_multiArmorAlias_accumulatesHp() {
        Zombie zombie = ZombieFactory.create("ZombieDarkArmor3", 0, 0);
        assertNotNull(zombie.getArmor());
        // Combined crown + shoulder armor should exceed either piece alone (both > 1000 each per JSON)
        assertTrue(zombie.getArmor().getArmorHp() >= 1600);
    }

    // ====== DAMAGE / DEFENSE INTERPLAY ======

    @Test
    @DisplayName("✅ Armor absorbs damage before body HP is touched")
    void takeDamage_armorAbsorbsBeforeHp() {
        Zombie zombie = ZombieFactory.create("ZombieArmor1", 0, 0); // Cone: 370 hp
        int bodyHpBefore = zombie.getHp();

        zombie.takeDamage(100, (Object) null);

        assertEquals(bodyHpBefore, zombie.getHp()); // body untouched
        assertEquals(270, zombie.getArmor().getArmorHp()); // 370 - 100
    }

    @Test
    @DisplayName("✅ Damage overflow after armor destruction reduces body HP")
    void takeDamage_overflowsToBodyAfterArmorBreaks() {
        Zombie zombie = ZombieFactory.create("ZombieArmor1", 0, 0); // Cone: 370 hp
        int bodyHpBefore = zombie.getHp();

        zombie.takeDamage(400, (Object) null); // 370 absorbed, 30 overflow

        assertTrue(zombie.getArmor() == null || zombie.getArmor().isDestroyed());
        assertEquals(bodyHpBefore - 30, zombie.getHp());
    }

    @Test
    @DisplayName("✅ Zombie dies when body HP reaches zero")
    void takeDamage_killsZombie_atZeroHp() {
        Zombie zombie = ZombieFactory.create("ZombieDefault", 0, 0);
        int hp = zombie.getHp();

        zombie.takeDamage(hp, (Object) null);

        assertFalse(zombie.isAlive());
        assertEquals(0, zombie.getHp());
    }

    @Test
    @DisplayName("❌ Invulnerable zombie takes zero damage")
    void takeDamage_invulnerable_takesNoDamage() {
        Zombie zombie = ZombieFactory.create("ZombieDefault", 0, 0);
        zombie.setVulnerabilityState(com.ussr.pvz.model.entities.zombies.Vulnerability.INVULNERABLE);
        int hpBefore = zombie.getHp();

        zombie.takeDamage(9999, (Object) null);

        assertEquals(hpBefore, zombie.getHp());
        assertTrue(zombie.isAlive());
    }

    // ====== COST LOOKUPS (used by wave spawning) ======

    @Test
    @DisplayName("✅ getZombieCost returns a positive cost for known aliases")
    void getZombieCost_knownAlias_returnsPositiveCost() {
        int cost = ZombieFactory.getZombieCost("ZombieDefault");
        assertTrue(cost > 0);
    }

    @Test
    @DisplayName("❌ getZombieCost returns MAX_VALUE for unknown aliases (prevents accidental free spawns)")
    void getZombieCost_unknownAlias_returnsMaxValue() {
        int cost = ZombieFactory.getZombieCost("TotallyFakeZombie");
        assertEquals(Integer.MAX_VALUE, cost);
    }

    @Test
    @DisplayName("✅ Higher-tier zombies cost more than the default zombie")
    void getZombieCost_gargantuar_costsMoreThanDefault() {
        int defaultCost = ZombieFactory.getZombieCost("ZombieDefault");
        int gargCost = ZombieFactory.getZombieCost("ZombieGargantuar");

        assertTrue(gargCost > defaultCost);
    }
}