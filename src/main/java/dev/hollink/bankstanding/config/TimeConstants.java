package dev.hollink.bankstanding.config;

import java.time.Duration;

public interface TimeConstants
{
	Duration GRACE_PERIOD_GRINDING = Duration.ofMinutes(3);
	Duration GRACE_PERIOD_MOVEMENT = Duration.ofMinutes(2);
	Duration GRACE_PERIOD_CHATTING = Duration.ofMinutes(2);

	Duration TIME_TILL_INITIAL_EXP = Duration.ofMinutes(5);
	Duration TIME_BETWEEN_DROPS = Duration.ofSeconds(30);
}
