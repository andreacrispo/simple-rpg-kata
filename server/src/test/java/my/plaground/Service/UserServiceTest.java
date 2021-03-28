package my.plaground.Service;

import my.plaground.Domain.CharacterClass;
import my.plaground.Domain.Entity.CharacterEntity;
import my.plaground.Domain.Entity.UserEntity;
import my.plaground.Repository.CharacterRepository;
import my.plaground.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
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

    @BeforeEach
    public void setup(){
        u1 = UserEntity.builder().id(1).username("Pippo").build();
        when(repository.findById(1)).thenReturn(Optional.of(u1));
        when(repository.findByUsername("Pippo")).thenReturn(u1);
    }

    @Test public void
    ensure_existing_user_is_logged_in()
    {
        assertTrue(this.service.login("Pippo", CharacterClass.Paladin));
    }

    @Test public void
    ensure_not_existing_user_is_not_logged_in(){
        assertFalse(this.service.login("NotExistingUser", CharacterClass.Paladin));
    }

}