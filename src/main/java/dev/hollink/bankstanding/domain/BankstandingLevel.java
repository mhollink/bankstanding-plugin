package dev.hollink.bankstanding.domain;

import static java.lang.Double.parseDouble;
import lombok.Getter;
import net.runelite.api.Experience;

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

	public boolean gainExperience(double exp)
	{
		int oldExpLevel = Experience.getLevelForXp((int) experience);
		int newLevel = Experience.getLevelForXp((int)(experience + exp));

		this.experience += exp;

		return newLevel > oldExpLevel;
	}

	public int getCurrentLevel()
	{
		return Experience.getLevelForXp((int) experience);
	}

}
