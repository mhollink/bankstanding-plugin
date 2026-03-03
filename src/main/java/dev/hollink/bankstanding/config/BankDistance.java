package dev.hollink.bankstanding.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankDistance
{
	INSIDE(2.0, Integer.MIN_VALUE, 1),
	VERY_CLOSE(1.6, 2, 4),
	CLOSE(1.2, 5, 7),
	NEAR(0.6, 8, 12),
	FAR(0.1, 13, 20),
	NOWHERE_NEAR(0.05, 21, Integer.MAX_VALUE);

	final double expMultiplier;
	final int minDistance;
	final int maxDistance;

	public static BankDistance fromDistance(int distance)
	{
		for (BankDistance value : values())
		{
			if (distance >= value.minDistance && distance <= value.maxDistance)
			{
				return value;
			}
		}
		return NOWHERE_NEAR;
	}
}