package dev.hollink.bankstanding.constant;

public interface ExperienceConstants
{
	/**
	 * A base exp rate of 50 exp per 30 secondes.
	 * This results in the following MAX exp/h:
	 * <p>
	 * 50xp * 1.2 * 2.0 * 120 = 14400
	 * <p>
	 * With ~15K exp/h it will take
	 * - 36 days of doing nothing to get 99.
	 * - 555 dasy of doing nothing to get 200 M
	 */
	 int BASE_EXPERIENCE = 50;
}
