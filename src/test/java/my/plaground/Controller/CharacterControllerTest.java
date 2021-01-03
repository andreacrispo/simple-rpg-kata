package my.plaground.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import my.plaground.Character;
import my.plaground.Position;
import my.plaground.SimpleRpgKataApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = CharacterController.class)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    @Test public void
    ensure_that_position_is_correct_for_specific_char() throws Exception {
        Character characterToFind = SimpleRpgKataApplication.getMockedCharacterList().stream().filter(c -> c.getId() == 1).findFirst().orElse(null);

        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/1/position")).andReturn();
        String jsonResult = result.getResponse().getContentAsString();

        assertEquals(objectMapper.writeValueAsString(characterToFind.getPosition()), jsonResult);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test public void
    ensure_that_position_is_correctly_updated() throws Exception {
        Character characterToFind = SimpleRpgKataApplication.getMockedCharacterList().stream().filter(c -> c.getId() == 1).findFirst().orElse(null);
        Position newPosition = Position.at(characterToFind.getPosition().getX()+2, characterToFind.getPosition().getY()+ 3);

        var request = put("/api/character/1/position")
                .contentType(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsBytes(newPosition));
        MvcResult result = this.mockMvc.perform(request).andReturn();

        String jsonResult = result.getResponse().getContentAsString();

        assertEquals(objectMapper.writeValueAsString(newPosition), jsonResult);
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test public void
    ensure_characters_list_is_valid_and_not_empty() throws Exception {
        MvcResult result = this.mockMvc.perform(MockMvcRequestBuilders.get("/api/character/all")).andReturn();
        String jsonResult = result.getResponse().getContentAsString();

        List resultList = this.objectMapper.readValue(jsonResult, List.class);

        assertTrue(resultList != null && !resultList.isEmpty());
    }
}