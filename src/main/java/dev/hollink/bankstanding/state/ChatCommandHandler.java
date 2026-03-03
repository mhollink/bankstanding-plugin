package dev.hollink.bankstanding.state;

import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;


@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ChatCommandHandler
{
	private final Client client;
	private final BankstandingExperienceManager xpManager;

	private void sendLevelResponse(ChatMessage chatMessage, String label, int level, double experience)
	{
		String playerName = client.getLocalPlayer().getName();
		if (playerName == null)
		{
			return;
		}
		String response = new ChatMessageBuilder()
			.append(ChatColorType.NORMAL)
			.append("Level ")
			.append(ChatColorType.HIGHLIGHT)
			.append(label).append(": ")
			.append(String.valueOf(level))
			.append(ChatColorType.NORMAL)
			.append(" Experience: ")
			.append(ChatColorType.HIGHLIGHT)
			.append(String.valueOf(experience))
			.append(ChatColorType.NORMAL)
			.build();

		log.debug("Setting response {}", response);
		final MessageNode messageNode = chatMessage.getMessageNode();
		messageNode.setRuneLiteFormatMessage(response);
		client.refreshChat();
	}

	public void handleLevelCommand(ChatMessage chatMessage, String message)
	{
		String[] args = message.split(" ");
		log.debug("Received level command: {}", Arrays.toString(args));
		if (args.length != 2)
		{
			return;
		}

		String category = args[1].toLowerCase();
		log.debug("Received level command: {}", category);
		switch (category)
		{
			case "bs":
			case "bankstanding":
				sendLevelResponse(chatMessage, "Bankstanding", xpManager.getBankstanding().getCurrentLevel(), xpManager.getBankstanding().getExperience());
				break;
		}
	}
}
