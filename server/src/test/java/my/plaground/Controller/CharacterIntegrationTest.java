package my.plaground.Controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.plaground.Domain.Character;
import my.plaground.Domain.Paladin;
import my.plaground.Domain.Position;
import my.plaground.Service.CharacterService;
import my.plaground.Domain.Wizard;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Arrays;
import java.util.List;

import static my.plaground.Domain.Position.at;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
public class CharacterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private CharacterService service;


    @Test
    public void
    ensure_that_position_is_correct_for_specific_char() throws Exception {
        Character characterToFind = new Paladin();
        characterToFind.setPosition(at(8,2));
        characterToFind.setId(100);

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/100/position")).andReturn();
        String jsonResult = result.getResponse().getContentAsString();

        assertEquals(objectMapper.writeValueAsString(characterToFind.getPosition()), jsonResult);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }


    @Test
    public void
    ensure_throws_exception_if_character_id_is_invalid() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/-1/position")).andReturn();
        String jsonResult = result.getResponse().getContentAsString();

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getResponse().getStatus());
    }

    @Test public void
    ensure_characters_list_is_valid_and_not_empty() throws Exception {

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/all")).andReturn();
        String jsonResult = result.getResponse().getContentAsString();

        var resultList = this.objectMapper.readValue(jsonResult, List.class);

        assertTrue(resultList != null && !resultList.isEmpty());
    }

    @Test public void
    ensure_characters_alive_list_is_valid_and_not_empty() throws Exception {

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/all/alive")).andReturn();
        String jsonResult = result.getResponse().getContentAsString();


        List<Character> resultList = Arrays.asList( this.objectMapper.readValue(jsonResult, Paladin[].class));
        boolean areAllAlive =  resultList.stream().allMatch(Character::isAlive);
        assertFalse(resultList.isEmpty());
        assertTrue(areAllAlive);
    }

    @Test public void
    ensure_existing_character_is_found() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/100")).andReturn();
        String jsonResult = result.getResponse().getContentAsString();

        Paladin characterFound = this.objectMapper.readValue(jsonResult, Paladin.class);

        assertEquals(characterFound.getId(), 100);
    }

    @Test public void
    ensure_that_character_in_range_can_attack_target() throws Exception {
        MvcResult attackRequest = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/1001/attack/1002")).andReturn();
        MvcResult characterOneRequest = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/1001")).andReturn();
        MvcResult CharacterTwoRequest = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/1002")).andReturn();

        Paladin paladin = this.objectMapper.readValue(characterOneRequest.getResponse().getContentAsString(), Paladin.class);
        Wizard wizard = this.objectMapper.readValue(CharacterTwoRequest.getResponse().getContentAsString(), Wizard.class);

        assertEquals(paladin.getHp(), 150);
        assertTrue(wizard.getHp() <  120);
    }

    @Test public void
    ensure_that_character_not_in_range_cannot_attack_target() throws Exception {
        MvcResult attackRequest = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/1003/attack/1004")).andReturn();
        MvcResult characterOneRequest = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/1003")).andReturn();
        MvcResult CharacterTwoRequest = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/1004")).andReturn();

        Paladin paladin = this.objectMapper.readValue(characterOneRequest.getResponse().getContentAsString(), Paladin.class);
        Wizard wizard = this.objectMapper.readValue(CharacterTwoRequest.getResponse().getContentAsString(), Wizard.class);

        assertEquals(paladin.getHp(),150);
        assertEquals(wizard.getHp(), 120);
    }

    @Test public void
    ensure_dead_character_is_respawned() throws Exception {
        MvcResult respawnRequest = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/respawn/2002")).andReturn();
        MvcResult respawnedCharacterRequest = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/2002")).andReturn();

        Wizard wizard = this.objectMapper.readValue(respawnedCharacterRequest.getResponse().getContentAsString(), Wizard.class);

        assertEquals(HttpStatus.OK.value(), respawnRequest.getResponse().getStatus());
        assertNotEquals(0, wizard.getHp());
    }
}
