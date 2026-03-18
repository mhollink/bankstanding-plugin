package dev.hollink.bankstanding.state;

import dev.hollink.bankstanding.state.level.LevelCommandHandler;
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

	public void handleLevelCommand(ChatMessage chatMessage, String message)
	{
		levelCommandHandler.handleCommand(chatMessage, message);
	}
}
