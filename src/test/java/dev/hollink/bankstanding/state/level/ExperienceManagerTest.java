package dev.hollink.bankstanding.state.level;

import dev.hollink.bankstanding.config.ActivityState;
import dev.hollink.bankstanding.config.BankDistance;
import dev.hollink.bankstanding.domain.PlayerState;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import dev.hollink.bankstanding.events.BankstandingExperienceGainedEvent;
import dev.hollink.bankstanding.events.BankstandingPlayerStateChangedEvent;
import java.time.Instant;
import java.util.function.Consumer;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.config.ConfigManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static dev.hollink.bankstanding.config.BankLocation.GRAND_EXCHANGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExperienceManagerTest
{
	private Client mockClient;
	private ConfigManager mockConfigManager;
	private BankstandingEventBus mockEventBus;
	private Player mockPlayer;

	private ExperienceManager experienceManager;

	@Before
	public void setUp()
	{
		mockClient = mock(Client.class);
		mockConfigManager = mock(ConfigManager.class);
		mockEventBus = mock(BankstandingEventBus.class);

		mockPlayer = mock(Player.class);
		when(mockPlayer.getName()).thenReturn("TestPlayer");
		when(mockPlayer.getWorldLocation()).thenReturn(GRAND_EXCHANGE.centerPoint);
		when(mockClient.getLocalPlayer()).thenReturn(mockPlayer);

		experienceManager = new ExperienceManager(mockClient, mockConfigManager, mockEventBus);
	}

	@After
	public void tearDown()
	{
		reset(mockClient, mockConfigManager, mockEventBus);
	}

	@Test
	public void init_shouldRegisterListener()
	{
		experienceManager.init();
		verify(mockEventBus).register(any());
	}

	@Test
	public void destroy_shouldUnregisterListener()
	{
		experienceManager.destroy();
		verify(mockEventBus).unregister(any());
	}

	@Test
	public void startUp_shouldLoadStoredExperience()
	{
		when(mockConfigManager.getRSProfileConfiguration(anyString(), anyString()))
			.thenReturn("1500");

		experienceManager.startUp();

		assertEquals(1500.0, experienceManager.getBankstanding().getExperience(), 0.01);
	}

	@Test
	public void listener_shouldUpdateStateFields_onStateChangeEvent()
	{
		experienceManager.init();

		Consumer<BankstandingEvent> listener = getRegisteredListener();

		ActivityState oldActivity = ActivityState.LOAFING;
		Instant startTime = Instant.now();
		ActivityState newActivity = ActivityState.AFK;
		Instant newTimestamp = startTime.plusSeconds(100);

		BankstandingPlayerStateChangedEvent mockEvent = mock(BankstandingPlayerStateChangedEvent.class);
		when(mockEvent.getOldState()).thenReturn(new PlayerState(startTime, newActivity));
		when(mockEvent.getNewState()).thenReturn(new PlayerState(newTimestamp, oldActivity));

		listener.accept(mockEvent);

		assertEquals(oldActivity, experienceManager.getLastState());
		assertEquals(newTimestamp, experienceManager.getLastStateChange());
		assertEquals(newTimestamp, experienceManager.getLastExpDrop());
	}

	@Test
	public void listener_shouldDoNothing_ifEventIsNullOrWrongType()
	{
		experienceManager.init();

		Consumer<BankstandingEvent> listener = getRegisteredListener();

		ActivityState initialState = experienceManager.getLastState();
		Instant initialExpDrop = experienceManager.getLastExpDrop();
		Instant initialStateChange = experienceManager.getLastStateChange();

		listener.accept(null);
		listener.accept(mock(BankstandingEvent.class));

		assertEquals(initialState, experienceManager.getLastState());
		assertEquals(initialExpDrop, experienceManager.getLastExpDrop());
		assertEquals(initialStateChange, experienceManager.getLastStateChange());
	}

	@Test
	public void grantExperience_shouldIncreaseExperience_publishEventAndUpdateConfig()
	{
		experienceManager.startUp();

		when(mockClient.getLocalPlayer()).thenReturn(mockPlayer);

		experienceManager.grantExperience(ActivityState.AFK);

		verify(mockConfigManager).setRSProfileConfiguration(
			eq("Bankstanding"),
			eq("current_experience"),
			anyString()
		);

		ArgumentCaptor<BankstandingEvent> captor = ArgumentCaptor.forClass(BankstandingEvent.class);
		verify(mockEventBus).publish(captor.capture());
		BankstandingEvent event = captor.getValue();
		assertTrue(event instanceof BankstandingExperienceGainedEvent);

		double experience = experienceManager.getBankstanding().getExperience();

		assertTrue(experience > 0);
	}

	@Test
	public void getBankDistance_shouldReturnNowhereNear_ifNoPlayer()
	{
		when(mockClient.getLocalPlayer()).thenReturn(null);

		BankDistance bankDistance = experienceManager.getBankDistance();

		assertEquals(BankDistance.NOWHERE_NEAR, bankDistance);
	}

	@Test
	public void getBankDistance_shouldReturnSomeDistance_whenPlayerPresent()
	{
		var mockPlayer = mock(Player.class);
		when(mockPlayer.getName()).thenReturn("TestPlayer");
		when(mockPlayer.getWorldLocation()).thenReturn(GRAND_EXCHANGE.centerPoint);
		when(mockClient.getLocalPlayer()).thenReturn(mockPlayer);

		BankDistance bankDistance = experienceManager.getBankDistance();

		assertEquals(BankDistance.INSIDE, bankDistance);
	}

	private Consumer<BankstandingEvent> getRegisteredListener()
	{
		ArgumentCaptor<Consumer<BankstandingEvent>> captor =
			ArgumentCaptor.forClass(Consumer.class);
		verify(mockEventBus).register(captor.capture());
		return captor.getValue();
	}
}