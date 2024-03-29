package my.plaground.Controller;


import lombok.extern.slf4j.Slf4j;
import my.plaground.Domain.Character;
import my.plaground.Exception.ResourceNotFound;
import my.plaground.Domain.Position;
import my.plaground.Service.CharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/character")
public class CharacterController {

    private final CharacterService characterService;

    @Autowired
    public CharacterController(CharacterService characterService) {
        this.characterService = characterService;
    }

    @GetMapping("/{characterId}")
    public ResponseEntity<Character> getCharacter(@PathVariable Integer characterId) throws Exception {
        Character character = this.characterService.getCharacter(characterId).orElseThrow(ResourceNotFound::new);
        return ResponseEntity.ok(character);
    }

    @GetMapping("/{characterId}/position")
    public ResponseEntity<Position> getCharacterPosition(@PathVariable Integer characterId) throws Exception {
       Character character = this.characterService.getCharacter(characterId).orElseThrow(ResourceNotFound::new);
        return ResponseEntity.ok(character.getPosition());
    }

//    @PutMapping("/{characterId}/position")
//    public ResponseEntity<Position> updateCharacterPosition(@PathVariable Integer characterId, @RequestBody Position newPosition) throws Exception {
//        Character character = characterService.updatePosition(characterId, newPosition, );
//        return ResponseEntity.ok(character.getPosition());
//    }

    @GetMapping("/all")
    public ResponseEntity<List<Character>> getCharacterList() throws Exception {
        return ResponseEntity.ok(this.characterService.getCharacters());
    }

    @GetMapping("/all/alive")
    public ResponseEntity<List<Character>> getCharacterAliveList() throws Exception {
        return ResponseEntity.ok(this.characterService.getCharactersAlive());
    }

    @GetMapping("/all/connected")
    public ResponseEntity<List<Character>> getCharacterConnectedList() throws Exception {
        return ResponseEntity.ok(this.characterService.getCharactersConnected());
    }

    @GetMapping("/{characterId}/attack/{targetId}")
    public ResponseEntity<Void> attackCharacter(@PathVariable Integer characterId, @PathVariable Integer targetId){
        characterService.attack(characterId, targetId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/respawn/{characterId}")
    public ResponseEntity<Void> respawn(@PathVariable Integer characterId){
        if(this.characterService.respawn(characterId))
            return ResponseEntity.ok().build();

        return ResponseEntity.badRequest().build();
    }

}