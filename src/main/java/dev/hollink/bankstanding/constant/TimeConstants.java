package dev.hollink.bankstanding.constant;

import java.time.Duration;

public interface TimeConstants
{
	Duration GRACE_PERIOD_GRINDING = Duration.ofSeconds(30);
	Duration GRACE_PERIOD_MOVEMENT = Duration.ofSeconds(30);
	Duration GRACE_PERIOD_CHATTING = Duration.ofSeconds(10);

	Duration TIME_TILL_INITIAL_EXP = Duration.ofSeconds(10);
	Duration TIME_BETWEEN_DROPS = Duration.ofSeconds(5);
}
