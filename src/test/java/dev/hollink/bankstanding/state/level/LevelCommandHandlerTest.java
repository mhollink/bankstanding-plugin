package dev.hollink.bankstanding.state.level;

import dev.hollink.bankstanding.domain.BankstandingLevel;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
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
	private ExperienceManager mockXpManager;
	private LevelCommandHandler commandHandler;

	private Player mockPlayer;
	private ChatMessage mockChatMessage;
	private MessageNode mockMessageNode;

	@Before
	public void setUp()
	{
		mockClient = mock(Client.class);
		mockXpManager = mock(ExperienceManager.class);
		commandHandler = new LevelCommandHandler(mockClient, mockXpManager);

		mockChatMessage = mock(ChatMessage.class);
		mockMessageNode = mock(MessageNode.class);
		when(mockChatMessage.getMessageNode()).thenReturn(mockMessageNode);

		mockPlayer = mock(Player.class);
		when(mockPlayer.getName()).thenReturn("TestPlayer");
		when(mockClient.getLocalPlayer()).thenReturn(mockPlayer);
	}

	@Test
	public void handleCommand_shouldSendLevelResponse_forBankstanding()
	{
		BankstandingLevel level = new BankstandingLevel(10);
		when(mockXpManager.getBankstanding()).thenReturn(level);

		commandHandler.handleCommand(mockChatMessage, "!level bankstanding");

		verify(mockMessageNode).setRuneLiteFormatMessage(
			contains("Bankstanding")
		);

		verify(mockClient).refreshChat();
	}

	@Test
	public void handleCommand_shouldSendLevelResponse_forBsShortcut()
	{
		BankstandingLevel level = new BankstandingLevel(15);
		when(mockXpManager.getBankstanding()).thenReturn(level);

		commandHandler.handleCommand(mockChatMessage, "!level bs");

		verify(mockMessageNode).setRuneLiteFormatMessage(
			contains("Bankstanding")
		);
		verify(mockClient).refreshChat();
	}

	@Test
	public void handleCommand_shouldNotSendMessage_forUnknownCategory()
	{
		BankstandingLevel level = new BankstandingLevel(10);
		when(mockXpManager.getBankstanding()).thenReturn(level);

		commandHandler.handleCommand(mockChatMessage, "!level unknown");

		verify(mockMessageNode, never()).setRuneLiteFormatMessage(any());
		verify(mockClient, never()).refreshChat();
	}

	@Test
	public void handleCommand_shouldIgnoreIncorrectArguments()
	{
		commandHandler.handleCommand(mockChatMessage, "!level");
		commandHandler.handleCommand(mockChatMessage, "!level a b c");

		verify(mockMessageNode, never()).setRuneLiteFormatMessage(any());
		verify(mockClient, never()).refreshChat();
	}
}