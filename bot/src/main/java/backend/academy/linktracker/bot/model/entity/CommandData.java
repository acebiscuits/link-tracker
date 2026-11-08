package backend.academy.linktracker.bot.model.entity;

import java.util.Map;

import backend.academy.linktracker.bot.exc.NoSuitabldeCommandResponseException;
import lombok.Builder;

@Builder
public record CommandData(
    String command, 
    String description, 
    Map<String, String> responses) {
    
    public String response(String key) {
        String response = responses.get(key);

        if (response == null) throw new NoSuitabldeCommandResponseException("No response for key: "+ key);

        return response;
    }
}
