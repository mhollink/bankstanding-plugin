package dev.hollink.bankstanding.state.level;

import dev.hollink.bankstanding.domain.BankstandingLevel;
import java.util.concurrent.ScheduledExecutorService;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.chat.ChatClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LevelCommandHandlerTest
{
	private Client mockClient;
	private ScheduledExecutorService mockExecutor;
	private ChatClient mockChatClient;
	private ExperienceManager mockXpManager;
	private LevelCommandHandler commandHandler;

	private Player mockPlayer;
	private ChatMessage mockChatMessage;
	private MessageNode mockMessageNode;

	@Before
	public void setUp()
	{
		mockClient = mock(Client.class);
		mockExecutor = mock(ScheduledExecutorService.class);
		mockChatClient = mock(ChatClient.class);
		mockXpManager = mock(ExperienceManager.class);
		commandHandler = new LevelCommandHandler(mockClient, mockExecutor, mockChatClient, mockXpManager);

		mockChatMessage = mock(ChatMessage.class);
		mockMessageNode = mock(MessageNode.class);
		when(mockChatMessage.getMessageNode()).thenReturn(mockMessageNode);
		when(mockChatMessage.getMessage()).thenReturn("!bankstanding");
		when(mockChatMessage.getType()).thenReturn(net.runelite.api.ChatMessageType.PRIVATECHATOUT);

		mockPlayer = mock(Player.class);
		when(mockPlayer.getName()).thenReturn("TestPlayer");
		when(mockClient.getLocalPlayer()).thenReturn(mockPlayer);
	}

	@Test
	public void handleCommand_shouldSendLevelResponse_forBankstanding()
	{
		BankstandingLevel level = new BankstandingLevel(10);
		when(mockXpManager.getBankstanding()).thenReturn(level);

		commandHandler.handleCommand(mockChatMessage, "!bankstanding");

		verify(mockMessageNode).setRuneLiteFormatMessage(
			contains("Bankstanding")
		);

		verify(mockClient).refreshChat();
	}

}