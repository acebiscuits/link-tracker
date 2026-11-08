package backend.academy.linktracker.bot.model.dto;

import com.pengrad.telegrambot.model.Update;

import lombok.Builder;

@Builder
public record UserMessageDto(long chatId, Update update) {
}