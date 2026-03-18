package dev.hollink.bankstanding.domain;

import lombok.Getter;
import net.runelite.api.Experience;

import static java.lang.Double.parseDouble;

@Getter
public class BankstandingLevel
{
	double experience;

	public BankstandingLevel(double experience)
	{
		this.experience = experience;
	}

	public BankstandingLevel(String experience)
	{
		this.experience = parseDouble(experience);
	}

	/**
	 * Add additional experience to the bankstanding level.
	 *
	 * @param exp the amount of exp added.
	 * @return boolean indicating if the player has leveled up from the given exp.
	 */
	public boolean gainExperience(double exp)
	{
		int oldExpLevel = Experience.getLevelForXp((int) experience);
		int newLevel = Experience.getLevelForXp((int) (experience + exp));

		this.experience += exp;

		return newLevel > oldExpLevel;
	}

	/**
	 * @return the current level capt at level 99.
	 */
	public int getCurrentLevel()
	{
		// Level clamped on 99. Use VLevel for virtual level going up till 126.
		return Math.min(99, Experience.getLevelForXp((int) experience));
	}

	/**
	 * @return the current level going up to 126.
	 */
	public int getCurrentVLevel()
	{
		return Experience.getLevelForXp((int) experience);
	}

}
