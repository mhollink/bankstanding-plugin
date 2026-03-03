package dev.hollink.bankstanding.constant;

import java.time.Duration;

public interface TimeConstants
{
	Duration GRACE_PERIOD_GRINDING = Duration.ofMinutes(1);
	Duration GRACE_PERIOD_MOVEMENT = Duration.ofMinutes(1);
	Duration GRACE_PERIOD_CHATTING = Duration.ofMinutes(1);

	Duration TIME_TILL_INITIAL_EXP = Duration.ofMinutes(1);
	Duration TIME_BETWEEN_DROPS = Duration.ofSeconds(30);

	Duration PROGRESS_PANEL_FADE_DELAY = Duration.ofMinutes(2);
}
