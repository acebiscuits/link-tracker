package backend.academy.linktracker.bot.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import backend.academy.linktracker.bot.model.entity.CommandData;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "bot-commands-properties")
@Getter
@Setter
public class CommandProperties {
    private Map<String, CommandData> commands;
}
