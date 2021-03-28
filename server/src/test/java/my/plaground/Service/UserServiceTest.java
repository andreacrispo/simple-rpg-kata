package my.plaground.Service;

import my.plaground.Domain.CharacterClass;
import my.plaground.Domain.Entity.CharacterEntity;
import my.plaground.Domain.Entity.UserEntity;
import my.plaground.Domain.Paladin;
import my.plaground.Domain.Rogue;
import my.plaground.Domain.Wizard;
import my.plaground.Domain.Character;
import my.plaground.Repository.CharacterRepository;
import my.plaground.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles({"test"})
public class UserServiceTest {

    @InjectMocks
    private UserService service;

    @Mock
    private UserRepository repository;

    @Mock
    private CharacterService characterService;

    @Mock
    private CharacterRepository characterRepository;

    @Spy
    private CharacterFactory factory;

    private UserEntity u1;
    private UserEntity u2;
    private UserEntity u999;
    private Character c1;
    private Character c2;
    private Character c3;

    @BeforeEach
    public void setup() {
        u1 = UserEntity.builder().id(1).username("Pippo").build();
        c1 = new Paladin();
        c2 = new Rogue();
        c3 =new Wizard();
        when(repository.findById(1)).thenReturn(Optional.of(u1));
        when(repository.findByUsername("Pippo")).thenReturn(u1);
        when(characterService.getCharactersByUser(u1)).thenReturn(List.of(c1,c2,c3));


        when(characterService.connect(Mockito.any(Character.class))).then(a -> {
            Character c = a.getArgument(0);
            c.setConnected(true);
            return c.isConnected();
        });
    }

    @Test
    public void
    ensure_existing_user_is_logged_in() {
        assertTrue(this.service.login("Pippo", CharacterClass.Paladin));
    }

    @Test
    public void
    ensure_not_existing_user_is_not_logged_in() {
        assertFalse(this.service.login("NotExistingUser", CharacterClass.Paladin));
    }


    @Test
    public void
    ensure_existing_user_can_log_out() {
        assertTrue(this.service.logout("Pippo"));
    }

    @Test
    public void
    ensure_not_existing_user_cannot_log_out() {
        assertFalse(this.service.logout("NotExistingUser"));
    }
}