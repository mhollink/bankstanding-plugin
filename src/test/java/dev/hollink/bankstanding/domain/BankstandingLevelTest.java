package dev.hollink.bankstanding.domain;

import static org.junit.Assert.*;

import org.junit.Test;
import net.runelite.api.Experience;

public class BankstandingLevelTest
{
	@Test
	public void gainExperience_shouldNotLevelUp_whenBelowThreshold()
	{
		int level = 10;
		int xpForLevel = Experience.getXpForLevel(level);
		int xpForNext = Experience.getXpForLevel(level + 1);

		double gain = (xpForNext - xpForLevel) - 1;

		BankstandingLevel b = new BankstandingLevel(xpForLevel);

		boolean leveledUp = b.gainExperience(gain);

		assertFalse(leveledUp);
		assertEquals(level, b.getCurrentLevel());
	}

	@Test
	public void gainExperience_shouldLevelUp_whenCrossingThreshold()
	{
		int level = 10;
		int xpForNext = Experience.getXpForLevel(level + 1);

		double startXp = xpForNext - 1;
		double gain = 2;

		BankstandingLevel b = new BankstandingLevel(startXp);

		boolean leveledUp = b.gainExperience(gain);

		assertTrue(leveledUp);
		assertEquals(level + 1, b.getCurrentLevel());
	}

	@Test
	public void gainExperience_shouldLevelUp_whenJumpingMultipleLevels()
	{
		int startLevel = 10;
		int targetLevel = 15;

		double startXp = Experience.getXpForLevel(startLevel);
		double gain = Experience.getXpForLevel(targetLevel) - startXp;

		BankstandingLevel b = new BankstandingLevel(startXp);

		boolean leveledUp = b.gainExperience(gain);

		assertTrue(leveledUp);
		assertEquals(targetLevel, b.getCurrentLevel());
	}

	@Test
	public void getCurrentLevel_shouldReturnLevel1_forVeryLowXp()
	{
		BankstandingLevel b = new BankstandingLevel(0);

		assertEquals(1, b.getCurrentLevel());
	}

	@Test
	public void getCurrentLevel_shouldReturnLevel92_forHalfWayXp()
	{
		BankstandingLevel b = new BankstandingLevel(6_517_253);

		assertEquals(92, b.getCurrentLevel());
	}

	@Test
	public void getCurrentLevel_shouldReturnLevel99_forMaxLevel()
	{
		BankstandingLevel b = new BankstandingLevel(13_034_431);

		assertEquals(99, b.getCurrentLevel());
	}

	@Test
	public void getCurrentLevel_shouldReturnLevel99_forMaxExp()
	{
		BankstandingLevel b = new BankstandingLevel(200_000_000);

		assertEquals(99, b.getCurrentLevel());
	}

	@Test
	public void getCurrentVLevel_shouldReturnLevel126_forMaxExp()
	{
		BankstandingLevel b = new BankstandingLevel(200_000_000);

		assertEquals(126, b.getCurrentVLevel());
	}
}