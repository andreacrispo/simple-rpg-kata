package my.plaground.Service;

import lombok.extern.slf4j.Slf4j;
import my.plaground.Domain.Character;
import my.plaground.Domain.CharacterClass;
import my.plaground.Domain.Entity.UserEntity;
import my.plaground.Exception.ResourceNotFound;
import my.plaground.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserService {
    private final UserRepository repository;
    private final CharacterService characterService;

    public UserService(UserRepository repository, CharacterService characterService) {
        this.repository = repository;
        this.characterService = characterService;
    }


    public boolean login(String username, CharacterClass characterClass) {
        UserEntity user = this.repository.findByUsername(username);
        if(user == null)
           return false;

        Character character = this.getCharacterByUsernameAndClassId(user, characterClass)
                                  .orElseGet(() -> createCharacterByUsernameAndClass(user, characterClass));

        if(character == null)
            return false;

        return this.characterService.connect(character);
    }


    public boolean logout(String username) {
        UserEntity user = this.repository.findByUsername(username);
        if(user == null)
            return false;

        List<Character> characters = this.characterService.getCharactersByUser(user);

        if(characters == null || characters.size() == 0)
            return false;

        characters.forEach(this.characterService::disconnect);

        return characters.stream().noneMatch(Character::isConnected);
    }


    private Optional<Character> getCharacterByUsernameAndClassId(UserEntity user, CharacterClass characterClass){
        List<Character> characters = this.characterService.getCharactersByUser(user);

        List<Character> selectedClassChars = characters.stream().filter(character1 -> character1.getCharacterClass() == characterClass).collect(Collectors.toList());
        if(selectedClassChars.size() > 1)
            log.warn("Multiple characters found for user: " + user.getUsername() + " class: " + characterClass.toString());

        return selectedClassChars.stream().findFirst();
    }

    private Character createCharacterByUsernameAndClass(UserEntity user, CharacterClass characterClass) {
        return this.characterService.createCharacter(user, characterClass);
    }
}
