package my.plaground.Service;

import my.plaground.Domain.Character;
import my.plaground.Domain.CharacterClass;
import my.plaground.Domain.Entity.UserEntity;
import my.plaground.Domain.MoveDirection;
import my.plaground.Domain.Position;
import my.plaground.Exception.ResourceNotFound;
import my.plaground.Domain.Entity.CharacterEntity;
import my.plaground.Repository.CharacterRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CharacterService {

    private final CharacterRepository repository;
    private final CharacterFactory characterFactory;

    public CharacterService(CharacterRepository repository, CharacterFactory characterFactory) {
        this.repository = repository;
        this.characterFactory = characterFactory;
    }

    public List<Character> getCharacters() {
        return this.repository.findAll().stream()
                    .map(this.characterFactory::getCharacter).collect(Collectors.toList());
    }

    public List<Character> getCharactersAlive() {
        return  this.getCharacters().stream()
                .filter(Character::isAlive).collect(Collectors.toList());
    }

    public List<Character> getCharactersConnected() {
        return  this.getCharacters().stream()
                .filter(Character::isConnected).collect(Collectors.toList());
    }

    public Optional<Character> getCharacter(int characterId){
        try {
            Optional<CharacterEntity> character = this.repository.findById(characterId);
            return character.map(this.characterFactory::getCharacter);
        } catch (EntityNotFoundException e){
            return Optional.empty();
        }
    }

    public Character updatePosition(int characterId, Position newPosition, MoveDirection moveDirection) {
        Optional<CharacterEntity> characterOptional = this.repository.findById(characterId);
        if(characterOptional.isPresent()) {
            CharacterEntity character = characterOptional.get();
            character.setPositionx(newPosition.getX());
            character.setPositiony(newPosition.getY());
            character.setMoveDirection(moveDirection);
            CharacterEntity charSaved = this.repository.save(character);

            return this.characterFactory.getCharacter(charSaved);
        }
        else
            return null;
    }

    public Character updateCharacter(Character updatedCharacter) {
        Optional<CharacterEntity> characterFromDb = this.repository.findById(updatedCharacter.getId());

        if(characterFromDb.isPresent()) {
            CharacterEntity character = characterFromDb.get();

            character.setPositionx(updatedCharacter.getPosition().getX());
            character.setPositiony(updatedCharacter.getPosition().getY());
            character.setHp(updatedCharacter.getHp());
            character.setLevelValue(updatedCharacter.getLevel());
            character.setConnected(updatedCharacter.isConnected());
            CharacterEntity charSaved = this.repository.save(character);

            return this.characterFactory.getCharacter(charSaved);
        }

        return null;
    }
    

    public void attack(int characterId, int targetId) {
        Optional<Character> attackerFromDB = this.getCharacter(characterId);
        Optional<Character> targetFromDB = this.getCharacter(targetId);

        if(attackerFromDB.isEmpty() || targetFromDB.isEmpty())
            throw new ResourceNotFound();

        Character attacker = attackerFromDB.get();
        Character target = targetFromDB.get();

        attacker.attack(target);

        this.updateCharacter(target);
    }

    public List<Character> getCharactersByUser(UserEntity user) {
        List<CharacterEntity> characters = this.repository.findByUser(user);
        return characters.stream().map(this.characterFactory::getCharacter).collect(Collectors.toList());
    }


    public Character createCharacter(UserEntity user, CharacterClass characterClass) {
        if(user == null || user.getUsername() == null)
            throw new ResourceNotFound();

        Character newChar = this.characterFactory.getCharacterByClass(characterClass);

        CharacterEntity entity = new CharacterEntity();
        entity.setClassId(characterClass);
        entity.setUser(user);
        entity.setHp(newChar.getHp());
        entity.setLevelValue(newChar.getLevel());
        entity.setPositionx(0);
        entity.setPositiony(0);

        this.repository.save(entity);

        return this.characterFactory.getCharacter(entity);
    }



    public void connect(Character character) {
        character.setConnected(true);
        this.updateCharacter(character);
    }

    public void disconnect(Character character) {
        character.setConnected(false);
        this.updateCharacter(character);
    }

    public Boolean respawn(Integer characterId) {
        return this.getCharacter(characterId)
                        .map(c ->{
                                    c.setHp(c.getInitHp());
                                    c.setPosition(Position.at(50, 100));
                                    updateCharacter(c);
                                    return true;
                                })
                        .orElseGet(() -> false);
    }
}
