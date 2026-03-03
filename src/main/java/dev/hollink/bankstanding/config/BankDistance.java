package dev.hollink.bankstanding.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankDistance
{
	INSIDE(2.0, Integer.MIN_VALUE, 0),
	VERY_CLOSE(1.6, 1, 2),
	CLOSE(1.2, 3, 6),
	NEAR(0.6, 7, 11),
	FAR(0.1, 12, 15),
	NOWHERE_NEAR(0.0, 16, Integer.MAX_VALUE);

	public final double expMultiplier;
	private final int minDistance;
	private final int maxDistance;

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