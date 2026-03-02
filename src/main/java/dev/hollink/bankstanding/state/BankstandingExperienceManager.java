package dev.hollink.bankstanding.state;

import static dev.hollink.bankstanding.BankstandingConfig.CONFIG_GROUP;
import static dev.hollink.bankstanding.BankstandingConfig.CURRENT_EXPERIENCE_CONFIG_KEY;
import dev.hollink.bankstanding.config.ActivityState;
import static dev.hollink.bankstanding.constant.ExperienceConstants.BASE_EXPERIENCE;
import static dev.hollink.bankstanding.constant.TimeConstants.TIME_BETWEEN_DROPS;
import static dev.hollink.bankstanding.constant.TimeConstants.TIME_TILL_INITIAL_EXP;
import dev.hollink.bankstanding.domain.BankstandingLevel;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import dev.hollink.bankstanding.events.BankstandingExperienceGainedEvent;
import dev.hollink.bankstanding.events.BankstandingPlayerStateChangedEvent;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankstandingExperienceManager
{
	private final ConfigManager configManager;
	private final BankstandingEventBus events;

	private Instant lastExpDrop;
	private ActivityState lastState;

	private BankstandingLevel bankstanding = new BankstandingLevel(0);

	public void init() {
		events.register(this::onEvent);
	}

	public void destroy()
	{
		events.unregister(this::onEvent);
	}

	public void startUp()
	{
		String storedExpValue = configManager.getRSProfileConfiguration(CONFIG_GROUP, CURRENT_EXPERIENCE_CONFIG_KEY);
		bankstanding = Optional.ofNullable(storedExpValue)
			.map(BankstandingLevel::new)
			.orElse(new BankstandingLevel(0));

		log.debug("BankstandingExperienceManager started with {} initial experience", bankstanding.getExperience());
	}

	private void onEvent(BankstandingEvent event) {
		if (event instanceof BankstandingPlayerStateChangedEvent) {
			onStateChange((BankstandingPlayerStateChangedEvent) event);
		}
	}

	private void onStateChange(BankstandingPlayerStateChangedEvent currentState)
	{
		if (currentState == null)
		{
			return;
		}

		ActivityState state = currentState.getNewState().getActivity();
		if (state != lastState)
		{
			log.debug("Changing state from {} to {}", lastState, state);
			lastState = state;
			lastExpDrop = currentState.getNewState().getSince();
			return;
		}

		Instant now = Instant.now();
		Duration timeInState = Duration.between(currentState.getNewState().getSince(), now);
		if (timeInState.compareTo(TIME_TILL_INITIAL_EXP) < 0)
		{
			return;
		}

		if (Duration.between(lastExpDrop, now).compareTo(TIME_BETWEEN_DROPS) >= 0)
		{
			grantExperience(state);
			lastExpDrop = now;
		}
	}

	private void grantExperience(ActivityState state)
	{
		double stateMultiplier = state.getExpMultiplier();
		double xpToGive = BASE_EXPERIENCE * stateMultiplier;

		// TODO: apply AFK depth + bank stateMultiplier here
		log.debug("Granting experience to {}", xpToGive);
		boolean hasLeveledUp = bankstanding.gainExperience(xpToGive);
		configManager.setRSProfileConfiguration(
			CONFIG_GROUP,
			CURRENT_EXPERIENCE_CONFIG_KEY,
			String.valueOf(bankstanding.getExperience())
		);

		events.publish(new BankstandingExperienceGainedEvent(bankstanding.getExperience(), hasLeveledUp));
	}
}