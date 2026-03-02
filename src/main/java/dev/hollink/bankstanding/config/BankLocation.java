package dev.hollink.bankstanding.config;

import net.runelite.api.coords.WorldPoint;

public enum BankLocation
{

	GRAND_EXCHANGE(3164, 3487, 8),
	DRAYNOR(3094, 3243, 3),
	;

	public final WorldPoint centerPoint;
	public final int size;


	BankLocation(int x, int y, int plane, int size)
	{
		this.centerPoint = new WorldPoint(x, y, plane);
		this.size = size;
	}

	BankLocation(int x, int y, int size)
	{
		this(x, y, 0, size);
	}
}
