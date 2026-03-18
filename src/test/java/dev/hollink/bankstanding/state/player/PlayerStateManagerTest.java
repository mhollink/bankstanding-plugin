package dev.hollink.bankstanding.state.player;

import dev.hollink.bankstanding.config.ActivityState;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import dev.hollink.bankstanding.events.BankstandingPlayerStateChangedEvent;
import java.time.Instant;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static dev.hollink.bankstanding.config.BankLocation.GRAND_EXCHANGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PlayerStateManagerTest
{
	private Client mockClient;
	private BankstandingEventBus mockEventBus;
	private PlayerStateManager playerStateManager;
	private Player mockPlayer;

	@Before
	public void setUp()
	{
		mockClient = mock(Client.class);
		mockEventBus = mock(BankstandingEventBus.class);

		mockPlayer = mock(Player.class);
		when(mockClient.getLocalPlayer()).thenReturn(mockPlayer);
		when(mockPlayer.getName()).thenReturn("TestPlayer");
		when(mockPlayer.getWorldLocation()).thenReturn(GRAND_EXCHANGE.centerPoint);

		playerStateManager = new PlayerStateManager(mockClient, mockEventBus);
		playerStateManager.startUp();
	}

	@Test
	public void onChatMessage_shouldUpdateLastChatMessage_ifFromPlayer()
	{
		when(mockPlayer.getName()).thenReturn("TestPlayer");

		ChatMessage mockMessage = mock(ChatMessage.class);
		when(mockMessage.getType()).thenReturn(net.runelite.api.ChatMessageType.PUBLICCHAT);
		when(mockMessage.getName()).thenReturn("TestPlayer");

		Instant before = playerStateManager.getLastChatMessage().getTime();

		playerStateManager.onChatMessage(mockMessage);

		Instant after = playerStateManager.getLastChatMessage().getTime();
		assertTrue(after.isAfter(before));
	}

	@Test
	public void onChatMessage_shouldNotUpdate_ifWrongPlayerOrType()
	{
		ChatMessage mockMessage = mock(ChatMessage.class);
		when(mockMessage.getType()).thenReturn(net.runelite.api.ChatMessageType.MODCHAT); // ignored type
		when(mockMessage.getName()).thenReturn("OtherPlayer");

		Instant before = playerStateManager.getLastChatMessage().getTime();

		playerStateManager.onChatMessage(mockMessage);

		Instant after = playerStateManager.getLastChatMessage().getTime();
		assertEquals(before, after);
	}

	@Test
	public void checkForStateChanges_shouldPublishEvent_whenActivityChanges()
	{
		when(mockClient.getOverallExperience()).thenReturn(100L);

		ArgumentCaptor<BankstandingEvent> captor = ArgumentCaptor.forClass(BankstandingEvent.class);

		playerStateManager.checkForStateChanges();

		verify(mockEventBus).publish(captor.capture());

		assertEquals(ActivityState.GRINDING, playerStateManager.getCurrentPlayerState().getActivity());
		assertTrue(captor.getValue() instanceof BankstandingPlayerStateChangedEvent);
	}

	@Test
	public void checkForStateChanges_shouldNotPublishEvent_ifActivitySame()
	{
		// the first check will update from NULL to AFK.
		playerStateManager.checkForStateChanges();
		ActivityState current = playerStateManager.getCurrentPlayerState().getActivity();
		reset(mockEventBus);

		playerStateManager.checkForStateChanges();

		verify(mockEventBus, never()).publish(any());
		assertEquals(current, playerStateManager.getCurrentPlayerState().getActivity());
	}

	@Test
	public void movementUpdate_shouldUpdateLastMovement_ifDistanceExceeded()
	{
		WorldPoint initialLocation = new WorldPoint(0, 0, 0);
		WorldPoint movedLocation = new WorldPoint(10, 0, 0);

		when(mockPlayer.getWorldLocation()).thenReturn(initialLocation);
		playerStateManager.startUp();

		when(mockPlayer.getWorldLocation()).thenReturn(movedLocation);

		Instant before = playerStateManager.getLastMovement().getTime();

		playerStateManager.checkForStateChanges();

		Instant after = playerStateManager.getLastMovement().getTime();
		assertTrue(after.isAfter(before));
	}
}