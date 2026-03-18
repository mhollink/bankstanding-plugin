package dev.hollink.bankstanding.overlay;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.domain.BankstandingLevel;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import dev.hollink.bankstanding.events.BankstandingExperienceGainedEvent;
import dev.hollink.bankstanding.state.level.ExperienceManager;
import dev.hollink.bankstanding.utils.ExperienceFormatter;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExperienceOverlayStateManager
{
	private final BankstandingEventBus events;
	private final BankstandingConfig config;
	private final ExperienceManager experienceManager;

	@Getter
	private Instant lastExpDrop;
	@Getter
	private int currentLvl;
	private int currentXp;
	private int xpToLevel;
	@Getter
	private float progress;

	public void init()
	{
		events.register(this::onEvent);
	}

	public void destroy()
	{
		events.unregister(this::onEvent);
	}

	public void startUp()
	{
		BankstandingLevel bankstanding = experienceManager.getBankstanding();
		updateInternalState(bankstanding.getCurrentLevel(), bankstanding.getExperience());
		lastExpDrop = Instant.EPOCH;
	}

	public boolean hasRecentlyGainedExp()
	{
		Duration timeSinceLastExp = timeSinceLastExp();
		Duration fadeTime = fadeTime();

		return timeSinceLastExp.compareTo(fadeTime) <= 0;
	}

	public String getCurrentXp()
	{
		return ExperienceFormatter.valueOfExp(currentXp, config.experienceNotation());
	}

	public String getXpToLevel()
	{
		return ExperienceFormatter.valueOfExp(xpToLevel, config.experienceNotation());
	}

	private void onEvent(BankstandingEvent event)
	{
		if (event instanceof BankstandingExperienceGainedEvent)
		{
			BankstandingExperienceGainedEvent bankstanding = (BankstandingExperienceGainedEvent) event;
			updateInternalState(bankstanding.getSkill().getCurrentLevel(), bankstanding.getSkill().getExperience());
		}
	}

	private void updateInternalState(int currentLevel, double currentExperience)
	{
		int xpAtStartOfLevel = Experience.getXpForLevel(currentLevel);
		int xpForNextLevel = Experience.getXpForLevel(currentLevel + 1);

		double xpIntoLevel = currentExperience - xpAtStartOfLevel;
		double xpRequiredForLevel = xpForNextLevel - xpAtStartOfLevel;

		if (xpRequiredForLevel <= 0)
		{
			return; // safety guard
		}

		// Clamp between 0 and 1 to avoid floating precision issues
		this.progress = Math.max(0f, Math.min(1f, (float) (xpIntoLevel / xpRequiredForLevel)));
		this.currentLvl = currentLevel;
		this.currentXp = (int) currentExperience;
		this.xpToLevel = (int) xpRequiredForLevel;
		this.lastExpDrop = Instant.now();
	}

	private Duration fadeTime()
	{
		int seconds = Math.max(config.panelFadeTime(), 10);
		return Duration.ofSeconds(seconds);
	}

	private Duration timeSinceLastExp()
	{
		return Duration.between(lastExpDrop, Instant.now());
	}
}
