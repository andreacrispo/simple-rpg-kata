package my.plaground;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class RogueTest {
    @Test
    public void is_new_rogue_successfully_initialized() {
        Character character = new Rogue();

        assertEquals(120, character.getHp());
        assertEquals(1, character.getPower());
        assertEquals(3, character.getResistance());
    }

    @Test
    public void ensure_that_rogue_damage_is_valid() {
        Character character = new Rogue();
        int damage = character.attackDamage();
        assertTrue( damage >= 9 && damage <= 12, "not in range");
    }

    @ParameterizedTest
    @ValueSource(ints = {  1, 2 })
    public void ensure_that_rogue_empowered_damage_is_valid(int power){
        Character character =new Rogue(power);
        int empoweredDamage = character.empoweredDamage();

        assertTrue(empoweredDamage >= (9 * character.getPower()));
        assertTrue(empoweredDamage <= (12 * character.getPower()));
    }

    @ParameterizedTest
    @ValueSource(ints = { -1, 0 })
    public void ensure_that_rogue_with_invalid_power_throws_exception(int power){
        assertThrows(
                RuntimeException.class,
                () -> {
                    Character character = new Rogue(power);
                    int empoweredDamage = character.empoweredDamage();
                });
    }
}