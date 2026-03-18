package dev.hollink.bankstanding.state.level;

import dev.hollink.bankstanding.domain.BankstandingLevel;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import dev.hollink.bankstanding.events.BankstandingExperienceGainedEvent;
import dev.hollink.bankstanding.overlay.ConfettiOverlay;
import java.time.Duration;
import java.util.function.Consumer;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LevelUpHandlerTest
{
	private Client mockClient;
	private BankstandingEventBus mockEventBus;
	private ConfettiOverlay mockConfetti;

	private LevelUpHandler levelUpHandler;

	@Before
	public void setUp()
	{
		mockClient = mock(Client.class);
		mockEventBus = mock(BankstandingEventBus.class);
		mockConfetti = mock(ConfettiOverlay.class);

		levelUpHandler = new LevelUpHandler(mockClient, mockEventBus, mockConfetti);
	}

	@Test
	public void init_shouldRegisterListener()
	{
		levelUpHandler.init();

		verify(mockEventBus).register(any());
	}

	@Test
	public void destroy_shouldUnregisterListener()
	{
		levelUpHandler.destroy();

		verify(mockEventBus).unregister(any());
	}

	@Test
	public void event_shouldTriggerLevelUpBehavior_whenLeveledUp()
	{
		levelUpHandler.init();

		Consumer<BankstandingEvent> listener = getRegisteredListener();

		BankstandingLevel level = mock(BankstandingLevel.class);
		when(level.getCurrentLevel()).thenReturn(10);

		BankstandingExperienceGainedEvent xpEvent = mock(BankstandingExperienceGainedEvent.class);
		when(xpEvent.isLeveledUp()).thenReturn(true);
		when(xpEvent.getSkill()).thenReturn(level);

		listener.accept(xpEvent);

		verify(mockConfetti).trigger(Duration.ofSeconds(5));

		verify(mockClient).addChatMessage(
			eq(ChatMessageType.GAMEMESSAGE),
			eq(""),
			eq("You have leveled up your Bankstanding to level 10"),
			isNull()
		);

		verify(mockClient).playSoundEffect(SoundEffectID.GE_COIN_TINKLE);
	}

	@Test
	public void event_shouldNotTriggerLevelUp_whenNotLeveledUp()
	{
		levelUpHandler.init();

		Consumer<BankstandingEvent> listener = getRegisteredListener();

		BankstandingExperienceGainedEvent xpEvent = mock(BankstandingExperienceGainedEvent.class);
		when(xpEvent.isLeveledUp()).thenReturn(false);

		listener.accept(xpEvent);

		verify(mockConfetti, never()).trigger(any());
		verify(mockClient, never()).addChatMessage(any(), any(), any(), any());
		verify(mockClient, never()).playSoundEffect(anyInt());
	}

	private Consumer<BankstandingEvent> getRegisteredListener()
	{
		ArgumentCaptor<Consumer<BankstandingEvent>> captor =
			ArgumentCaptor.forClass(Consumer.class);
		verify(mockEventBus).register(captor.capture());
		return captor.getValue();
	}
}