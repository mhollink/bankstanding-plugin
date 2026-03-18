package dev.hollink.bankstanding.overlay;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.domain.BankstandingLevel;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import dev.hollink.bankstanding.events.BankstandingExperienceGainedEvent;
import dev.hollink.bankstanding.state.BankstandingExperienceManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.client.ui.overlay.OverlayPanel;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankstandingLevelProgressOverlay extends OverlayPanel implements OverlayHelper
{
	private final BankstandingEventBus events;
	private final BankstandingExperienceManager experienceManager;
	private final BankstandingConfig config;

	private Instant lastExpDrop;
	private int currentLvl;
	private int currentXp;
	private int xpToLevel;
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

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showBankstandingExperienceOverlay())
		{
			return super.render(graphics);
		}

		// Don't render if player is too far away from a bank region.
		if (experienceManager.getBankDistance().ordinal() > config.panelHideDistance().ordinal())
		{
			return super.render(graphics);
		}

		if (hasRecentlyGainedExp())
		{
			addExperienceOverlay();
		}

		return super.render(graphics);
	}

	private boolean hasRecentlyGainedExp()
	{
		Duration timeSinceLastExp = timeSinceLastExp();
		Duration fadeTime = fadeTime();

		return timeSinceLastExp.compareTo(fadeTime) <= 0;
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

	private void addExperienceOverlay()
	{
		setPanelWidth(160, panelComponent);
		addPanelPadding(panelComponent);
		addText("Bankstanding", String.valueOf(currentLvl), panelComponent);
		addLabelledText("Current xp:", valueOfExp(currentXp), panelComponent);
		addLabelledText("Xp to level:", valueOfExp(xpToLevel), panelComponent);
		addLineBreak(panelComponent);
		addProgressBar(progress, panelComponent);
	}


	private String valueOfExp(int exp)
	{
		switch (config.experienceNotation())
		{
			case K:
				return format(exp, 1_000, "K");
			case M:
				return format(exp, 1_000_000, "M");
			case AUTO:
				return autoFormat(exp);
			case FULL:
			default:
				return String.valueOf(exp);
		}
	}

	private String autoFormat(int exp)
	{
		if (exp >= 10_000_000)
		{
			return format(exp, 1_000_000, "M");
		}
		else if (exp >= 100_000)
		{
			return format(exp, 1_000, "K");
		}
		else
		{
			return String.valueOf(exp);
		}
	}

	private String format(int value, int divisor, String suffix)
	{
		double result = (double) value / divisor;
		return String.format("%.1f%s", result, suffix);
	}
}
