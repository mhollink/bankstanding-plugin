package dev.hollink.bankstanding.state;

import dev.hollink.bankstanding.state.level.LevelCommandHandler;
import dev.hollink.bankstanding.state.level.LevelSetCommandHandler;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ChatMessage;


@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatCommandHandler
{
	private final LevelCommandHandler levelCommandHandler;
	private final LevelSetCommandHandler setLevelCommandHandler;

	public void handleLevelCommand(ChatMessage chatMessage, String message)
	{
		levelCommandHandler.handleCommand(chatMessage, message);
	}

	public void handleSetLevelCommand(ChatMessage chatMessage, String message) {
		try {
			setLevelCommandHandler.handleCommand(chatMessage, message);
		}
		catch (IllegalArgumentException e) {
			log.warn(e.getMessage());
		}
	}
}
