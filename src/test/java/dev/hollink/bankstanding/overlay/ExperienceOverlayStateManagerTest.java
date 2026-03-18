package dev.hollink.bankstanding.overlay;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.config.ExperienceNotation;
import dev.hollink.bankstanding.domain.BankstandingLevel;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import dev.hollink.bankstanding.events.BankstandingExperienceGainedEvent;
import dev.hollink.bankstanding.state.level.ExperienceManager;
import java.util.function.Consumer;
import net.runelite.api.Experience;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExperienceOverlayStateManagerTest
{
	private BankstandingEventBus mockEventBus;
	private BankstandingConfig mockConfig;
	private ExperienceManager mockExperienceManager;

	private ExperienceOverlayStateManager stateManager;

	@Before
	public void setUp()
	{
		mockEventBus = mock(BankstandingEventBus.class);
		mockConfig = mock(BankstandingConfig.class);
		mockExperienceManager = mock(ExperienceManager.class);

		stateManager = new ExperienceOverlayStateManager(
			mockEventBus,
			mockConfig,
			mockExperienceManager
		);

		when(mockConfig.panelFadeTime()).thenReturn(10);
		when(mockConfig.showVirtualLevel()).thenReturn(false);
		when(mockConfig.experienceNotation()).thenReturn(ExperienceNotation.AUTO);
	}

	@Test
	public void init_shouldRegisterListener()
	{
		stateManager.init();

		verify(mockEventBus).register(any());
	}

	@Test
	public void destroy_shouldUnregisterListener()
	{
		stateManager.destroy();

		verify(mockEventBus).unregister(any());
	}

	@Test
	public void startUp_shouldInitializeState()
	{
		int lvl = 10;
		double xp = Experience.getXpForLevel(lvl);

		BankstandingLevel level = new BankstandingLevel(xp);
		when(mockExperienceManager.getBankstanding()).thenReturn(level);

		stateManager.startUp();

		assertEquals(lvl, stateManager.getCurrentLvl());
		assertEquals(0f, stateManager.getProgress(), 0.0001);
		assertEquals(java.time.Instant.EPOCH, stateManager.getLastExpDrop());
	}

	@Test
	public void event_shouldUpdateState_whenExperienceGained()
	{
		stateManager.init();

		Consumer<BankstandingEvent> listener = getRegisteredListener();

		int lvl = 10;
		double xp = Experience.getXpForLevel(lvl) + 100;

		BankstandingLevel level = new BankstandingLevel(xp);

		BankstandingExperienceGainedEvent event =
			mock(BankstandingExperienceGainedEvent.class);
		when(event.getSkill()).thenReturn(level);

		listener.accept(event);

		assertEquals(lvl, stateManager.getCurrentLvl());
		assertTrue(stateManager.getProgress() > 0);
		assertNotNull(stateManager.getLastExpDrop());
	}

	@Test
	public void event_shouldUseVirtualLevel_whenEnabled()
	{
		stateManager.init();

		when(mockConfig.showVirtualLevel()).thenReturn(true);

		Consumer<BankstandingEvent> listener = getRegisteredListener();

		BankstandingLevel level = mock(BankstandingLevel.class);
		when(level.getExperience()).thenReturn(10000.0);
		when(level.getCurrentVLevel()).thenReturn(99);

		BankstandingExperienceGainedEvent event =
			mock(BankstandingExperienceGainedEvent.class);
		when(event.getSkill()).thenReturn(level);

		listener.accept(event);

		assertEquals(99, stateManager.getCurrentLvl());
	}

	@Test
	public void hasRecentlyGainedExp_shouldBeTrue_afterEvent()
	{
		stateManager.init();

		Consumer<BankstandingEvent> listener = getRegisteredListener();

		BankstandingLevel level = new BankstandingLevel(10_000);
		BankstandingExperienceGainedEvent event = mock(BankstandingExperienceGainedEvent.class);
		when(event.getSkill()).thenReturn(level);

		listener.accept(event);

		assertTrue(stateManager.hasRecentlyGainedExp());
	}

	@Test
	public void hasRecentlyGainedExp_shouldBeFalse_afterStartupOnly()
	{
		BankstandingLevel level = new BankstandingLevel(0);
		when(mockExperienceManager.getBankstanding()).thenReturn(level);

		stateManager.startUp();

		assertFalse(stateManager.hasRecentlyGainedExp());
	}

	private Consumer<BankstandingEvent> getRegisteredListener()
	{
		ArgumentCaptor<Consumer<BankstandingEvent>> captor =
			ArgumentCaptor.forClass(Consumer.class);

		verify(mockEventBus).register(captor.capture());
		return captor.getValue();
	}
}