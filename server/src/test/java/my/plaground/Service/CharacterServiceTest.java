package my.plaground.Service;

import my.plaground.Domain.*;
import my.plaground.Domain.Character;
import my.plaground.Domain.Entity.CharacterEntity;
import my.plaground.Domain.Entity.UserEntity;
import my.plaground.Exception.ResourceNotFound;
import my.plaground.Repository.CharacterRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"test"})
public class CharacterServiceTest {

    @InjectMocks
    private CharacterService service;

    @Mock
    private CharacterRepository repository;

    @Spy
    private CharacterFactory factory;
    private UserEntity u1;
    private UserEntity u999;
    private CharacterEntity c1;
    private CharacterEntity c2;
    private CharacterEntity c3;

    @BeforeEach
    public void setup(){
        u1 = UserEntity.builder().id(1).build();
        u999 = UserEntity.builder().id(999).build();
        c1 = CharacterEntity.builder().id(1).hp(0).isConnected(false).levelValue(1).classId(CharacterClass.Paladin).user(u1).build();
        c2 = CharacterEntity.builder().id(2).hp(100).isConnected(true).levelValue(1).classId(CharacterClass.Wizard).user(u1).build();
        c3 = CharacterEntity.builder().id(3).hp(150).isConnected(true).levelValue(1).classId(CharacterClass.Rogue).user(u1).build();
        when(repository.findAll()).thenReturn(List.of(c1, c2, c3));
        when(repository.findById(1)).thenReturn(Optional.of(c1));
        when(repository.findById(2)).thenReturn(Optional.of(c2));
        when(repository.findById(3)).thenReturn(Optional.of(c3));
        when(repository.findById(998)).thenReturn(Optional.empty());
        when(repository.findById(999)).thenReturn(Optional.empty());
        when(repository.findByUser(u1)).thenReturn(List.of(c1, c2, c3));
        when(repository.findByUser(u999)).thenReturn(Collections.emptyList());
        when(repository.save(Mockito.any(CharacterEntity.class))).then(AdditionalAnswers.returnsFirstArg());
    }

    @Test public void
    ensure_getCharacters_does_not_return_null() {
        List<Character> characters = this.service.getCharacters();
        assertNotNull(characters);
    }


    @Test public void
    ensure_that_get_alive_characters_returns_only_alive_characters() {
        List<Character> aliveCharacters = this.service.getCharactersAlive();

       assertNotNull(aliveCharacters);
       assertTrue(aliveCharacters.size() > 0 && aliveCharacters.stream().allMatch(Character::isAlive) );
    }


    @Test public void
    ensure_that_get_connected_characters_returns_only_connected_characters() {
        List<Character> connectedCharacters = this.service.getCharactersConnected();

        assertNotNull(connectedCharacters);
        assertTrue(connectedCharacters.size() > 0 && connectedCharacters.stream().allMatch(Character::isConnected) );
    }

    @Test public void
    ensure_get_character_of_existing_character_returns_character_data() {
        Optional<Character> characterOptional = this.service.getCharacter(3);

        assertTrue(characterOptional.isPresent());

        Character character = characterOptional.get();

        assertEquals(3, character.getId());
        assertEquals(150, character.getHp());
        assertEquals(1, character.getLevel());
        assertEquals(CharacterClass.Rogue, character.getCharacterClass());
        assertTrue(character.isConnected());
    }

    @Test public void
    ensure_get_character_of_not_existing_character_returns_empty(){
        Optional<Character> characterOptional = this.service.getCharacter(999);
        assertTrue(characterOptional.isEmpty());
    }

    @Test public void
    ensure_update_position_of_not_existing_character_returns_null(){
        Character character = this.service.updatePosition(999, Position.at(0,0), MoveDirection.Down);
        assertNull(character);
    }

    @Test public void
    ensure_update_position_of_existing_character_returns_character_updated_data(){
        Character character = this.service.updatePosition(3, Position.at(99,55), MoveDirection.Down);

        assertEquals(99, character.getPosition().getX());
        assertEquals(55, character.getPosition().getY());
        assertEquals(MoveDirection.Down, character.getMoveDirection());
    }

    @Test public void
    ensure_update_not_existing_character_returns_null(){
        Character notExistingCharacter = new Paladin();
        notExistingCharacter.setId(999);

        Character character = this.service.updateCharacter(notExistingCharacter);
        assertNull(character);
    }

    @Test public void
    ensure_update_existing_character_returns_updated_data(){
        Character existingCharacter = this.service.getCharacters().get(0);
        existingCharacter.setHp(existingCharacter.getHp() - 1);
        existingCharacter.setLevel(existingCharacter.getLevel() + 1);
        existingCharacter.setConnected(!existingCharacter.isConnected());

        Character updatedCharacter = this.service.updateCharacter(existingCharacter);

        assertNotNull(updatedCharacter);
        assertEquals(existingCharacter.getHp(), updatedCharacter.getHp());
        assertEquals(existingCharacter.getLevel(), updatedCharacter.getLevel());
        assertEquals(existingCharacter.isConnected(), updatedCharacter.isConnected());
    }

    @Test public void
    ensure_not_existing_attacker_and_not_existing_enemy_throws_exception(){
        assertThrows(
                ResourceNotFound.class,
                () -> {
                    this.service.attack(998, 999);
                });
    }

    @Test public void
    ensure_not_existing_attacker_throws_exception(){
        assertThrows(
                ResourceNotFound.class,
                () -> {
                    this.service.attack(999, 1);
                });
    }

    @Test public void
    ensure_not_existing_enemy_throws_exception(){
        assertThrows(
                ResourceNotFound.class,
                () -> {
                    this.service.attack(1, 999);
                });
    }

    @Test public void
    ensure_not_existing_user_returns_no_characters(){
        List<Character> characters = this.service.getCharactersByUser(this.u999);

        assertNotNull(characters);
        assertTrue(characters.isEmpty());
    }
}