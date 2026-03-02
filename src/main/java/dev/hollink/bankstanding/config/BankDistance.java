package dev.hollink.bankstanding.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankDistance
{
	INSIDE(2.0, 0, 2),
	VERY_CLOSE(1.6, 3, 5),
	CLOSE(1.2, 6, 9),
	NEAR(0.6, 10, 15),
	FAR(0.1, 16, 25),
	NOWHERE_NEAR(0.05, 26, Integer.MAX_VALUE);

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