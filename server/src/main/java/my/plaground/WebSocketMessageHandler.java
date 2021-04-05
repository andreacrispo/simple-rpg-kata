package my.plaground;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import my.plaground.Domain.Character;
import my.plaground.Domain.DTO.WebSocketHandshake;
import my.plaground.Domain.Position;
import my.plaground.Domain.DTO.WebSocketParams;
import my.plaground.Service.CharacterService;
import my.plaground.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static my.plaground.Domain.ActionType.*;

@Component
public class WebSocketMessageHandler extends TextWebSocketHandler {

    private final CharacterService characterService;
    private final UserService userService;
    private ConcurrentHashMap<String, String> connectedUsers;

    @Autowired
    public WebSocketMessageHandler(CharacterService characterService, UserService userService){
        this.characterService = characterService;
        this.userService = userService;
        this.connectedUsers = new ConcurrentHashMap<>();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // The WebSocket has been closed
        String usernameToDisconnect =  this.connectedUsers.get(session.getId());
        this.userService.logout(usernameToDisconnect);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws InterruptedException, IOException {

        if(session.isOpen()) {
            try {
                WebSocketHandshake handshake = new WebSocketHandshake();
                handshake.setSessionId(session.getId());
                String messageToSend =  new ObjectMapper().writeValueAsString(handshake);
                session.sendMessage(new TextMessage(messageToSend));
            } catch (IOException e) { }
        }

        // The WebSocket has been opened
        // I might save this session object so that I can send messages to it outside of this method
        TimerTask timer  = new TimerTask() {
              @Override
              public void run() {
                  if(session.isOpen()) {
                      try {
                          String messageToSend = getConnectedCharactersMessage(session.getId());
                          session.sendMessage(new TextMessage(messageToSend));
                      } catch (IOException e) {
                      }
                  }else {
                    this.cancel();
                  }
              }
        };
        new Timer().scheduleAtFixedRate(timer, 0,600);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {

        try{
            WebSocketHandshake handshake = new ObjectMapper().readValue(textMessage.getPayload(), WebSocketHandshake.class);
            this.connectedUsers.putIfAbsent(handshake.getSessionId(), handshake.getUsername());
        }
        catch(Exception e){
        }

        try {
            WebSocketParams webSocketParams = new ObjectMapper().readValue(textMessage.getPayload(), WebSocketParams.class);

            switch(webSocketParams.getActionType())
            {
                case None:
                    break;
                case Movement:
                    this.characterService.updatePosition(webSocketParams.getCharacterId(), Position.at(webSocketParams.getPositionX(), webSocketParams.getPositionY()), webSocketParams.getMoveDirection());
                    break;
                case Attack:
                    this.characterService.attack(webSocketParams.getCharacterId(), webSocketParams.getTargetId());
                    break;
            }
        } catch (Exception e) {
        }
    }

    private String getConnectedCharactersMessage(String sessionId) throws JsonProcessingException {
        List<Character> characters = this.characterService.getCharactersConnected();
        List<WebSocketParams> webSocketResponse = characters.stream()
                .map(c -> this.toWebSocketParam(sessionId, c))
                .collect(Collectors.toList());

        return new ObjectMapper().writeValueAsString(webSocketResponse);
    }

    private WebSocketParams toWebSocketParam(String sessionId, Character c) {
        return new WebSocketParams(
                                sessionId,
                                c.getId(),
                                c.getPosition().getX(),
                                c.getPosition().getY(),
                                c.getMoveDirection(),
                                c.getHp(),
                                -1,
                                None,
                                c.getInitHp(),
                                c.getLevel(),
                                c.isConnected(),
                                c.getCharacterClass());
    }


}