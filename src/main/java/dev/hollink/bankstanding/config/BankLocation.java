package dev.hollink.bankstanding.config;

import net.runelite.api.coords.WorldPoint;

public enum BankLocation
{

	GRAND_EXCHANGE(3165, 3490, 8),
	VARROCK_WEST(3185, 3441, 3),
	VARROCK_EAST(3254, 3421, 3),
	EDGEVILE(3095, 3494, 3),
	COOKING_GUILD(3148, 3448, 0),
	FALADOR_EAST(3013, 3356, 3),
	FALADOR_WEST(2946, 3368, 2),
	DRAYNOR(3094, 3243, 3),
	TUTORIAL(3122, 3124, 2),
	AL_KHARID(3269, 3167, 2),
	EMIRS_ARENA(3384, 3270, 0),
	CRAFTING_GUILD(2936, 3280, 0),
	FEROX_ENCLAVE(3130, 3629, 3);

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
