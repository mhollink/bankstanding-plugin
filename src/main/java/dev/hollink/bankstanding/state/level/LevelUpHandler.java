package dev.hollink.bankstanding.state.level;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.domain.BankstandingLevel;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import dev.hollink.bankstanding.events.BankstandingExperienceGainedEvent;
import dev.hollink.bankstanding.overlay.ConfettiOverlay;
import java.time.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LevelUpHandler
{
	private final Client client;
	private final BankstandingEventBus events;
	private final ConfettiOverlay confetti;
	private final BankstandingConfig config;

	public void init()
	{
		events.register(this::onEvent);
	}

	public void destroy()
	{
		events.unregister(this::onEvent);
	}

	private void onEvent(BankstandingEvent event)
	{
		if (event instanceof BankstandingExperienceGainedEvent)
		{
			BankstandingExperienceGainedEvent xpEvent = (BankstandingExperienceGainedEvent) event;
			if (xpEvent.isLeveledUp())
			{
				levelUp(xpEvent);
			}
		}
	}

	private void levelUp(BankstandingExperienceGainedEvent xpEvent)
	{
		log.debug("Bankstanding has been level up to {}", xpEvent.getSkill().getCurrentLevel());
		if (config.playLevelUpSfx())
		{
			playLevelUpSound();
		}
		if (config.showLevelUpConfetti())
		{
			confetti.trigger(Duration.ofSeconds(5));
		}
		if (config.showLevelUpMessage())
		{
			sendLevelUpMessage(xpEvent.getSkill());
		}
	}

	private void sendLevelUpMessage(BankstandingLevel skill)
	{
		client.addChatMessage(
			ChatMessageType.GAMEMESSAGE,
			"",
			String.format("You have leveled up your Bankstanding to level %d", skill.getCurrentLevel()),
			null);
	}

	private void playLevelUpSound()
	{
		// TODO: Set a nicer sound effect
		client.playSoundEffect(SoundEffectID.GE_COIN_TINKLE);
	}
}
