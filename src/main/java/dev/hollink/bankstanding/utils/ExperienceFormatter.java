package dev.hollink.bankstanding.utils;

import dev.hollink.bankstanding.config.ExperienceNotation;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExperienceFormatter
{

	public  String valueOfExp(int exp, ExperienceNotation notation)
	{
		switch (notation)
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

	private String format(int value, int divisor, String suffix) {
		if (value < divisor) {
			return String.valueOf(value);
		}

		double result = (double) value / divisor;

		// Check if decimal part is zero
		if (result == Math.floor(result)) {
			return String.format("%.0f%s", result, suffix); // no decimal
		} else {
			return String.format("%.1f%s", result, suffix); // one decimal
		}
	}
}
