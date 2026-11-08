package backend.academy.linktracker.bot.mapper;

import org.springframework.stereotype.Component;

import backend.academy.linktracker.bot.exc.ScrapperClientException;
import backend.academy.linktracker.bot.model.dto.BotResponseDto;
import backend.academy.linktracker.bot.model.entity.CommandData;

@Component
public class ScrapperClientExceptionMapper {

    public BotResponseDto toBotResponseDto(Long chatId, CommandData commandData, ScrapperClientException ex) {
        String responseKey = switch(ex.getStatusCode()) {
            case 400 -> "bad-request";
            case 404 -> "not-found";
            case 409 -> "conflict";
            default -> "scrapper-error";
        };

        return new BotResponseDto(chatId, commandData.response(responseKey));
    }
}
