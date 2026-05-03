package dev.hollink.bankstanding.state.level;

import dev.hollink.bankstanding.state.CommandHandler;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.chat.ChatClient;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.events.ChatInput;
import net.runelite.client.util.Text;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LevelCommandHandler implements CommandHandler
{
	public static final String KC_NAME = "bankstanding-plugin.level";
	private final Client client;
	private final ScheduledExecutorService executor;
	private final ChatClient chatClient;
	private final ExperienceManager xpManager;

	private void sendLevelResponse(ChatMessage chatMessage, String label, int level, int experience)
	{
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

	public void handleCommand(ChatMessage chatMessage, String message)
	{
		ChatMessageType type = chatMessage.getType();
		final String player = type.equals(ChatMessageType.PRIVATECHATOUT)
			? client.getLocalPlayer().getName()
			: Text.sanitize(chatMessage.getName());

		try
		{
			log.debug("Looking up player level {}", player);
			int exp = chatClient.getKc(player, KC_NAME);
			int level = Math.min(99, Experience.getLevelForXp(exp));
			log.debug("Player {} has level {}. exp={}", player, level, exp);
			sendLevelResponse(chatMessage, "Bankstanding", level, exp);
		}
		catch (IOException ex)
		{
			log.debug("unable to lookup player level", ex);
		}
	}

	public boolean submitLevel(ChatInput chatInput, String s)
	{
		double experience = xpManager.getBankstanding().getExperience();
		log.debug("Received level submission {}", experience);

		if (experience <= 0)
		{
			return false;
		}

		executor.execute(() ->
		{
			try
			{
				String playerName = client.getLocalPlayer().getName();
				log.debug("Submitting exp {} for {}. {}", experience, playerName, KC_NAME);
				chatClient.submitKc(playerName, KC_NAME, (int) experience);
				log.debug("Submitted exp {} for {}. {}", experience, playerName, KC_NAME);
			}
			catch (Exception ex)
			{
				log.warn("unable to submit bankstanding level", ex);
			}
			finally
			{
				chatInput.resume();
			}
		});
		return true;
	}
}
