package dev.hollink.bankstanding.state;

import net.runelite.api.events.ChatMessage;

@FunctionalInterface
public interface CommandHandler
{
	void handleCommand(ChatMessage chatMessage, String message);
}
