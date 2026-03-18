package dev.hollink.bankstanding.utils;

import dev.hollink.bankstanding.config.BankDistance;
import dev.hollink.bankstanding.config.BankLocation;
import java.util.Optional;
import net.runelite.api.coords.WorldPoint;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BankDistanceFinderTest
{
	@Test
	public void getClosestBank_shouldReturnClosestBank()
	{
		WorldPoint testPoint = new WorldPoint(3092, 3245, 0);

		Optional<BankLocation> result = BankDistanceFinder.getCLosestBank(testPoint);

		assertTrue(result.isPresent());
		assertEquals(BankLocation.DRAYNOR, result.get());
	}

	@Test
	public void getClosestBank_shouldAlwaysReturnResult_whenBanksExist()
	{
		WorldPoint testPoint = new WorldPoint(0, 0, 0);

		Optional<BankLocation> result = BankDistanceFinder.getCLosestBank(testPoint);

		assertTrue(result.isPresent());
	}

	@Test
	public void getDistanceToBank_shouldReturnNowhereNear_whenBankIsNull()
	{
		WorldPoint point = new WorldPoint(0, 0, 0);

		BankDistance result = BankDistanceFinder.getDistanceToBank(null, point);

		assertEquals(BankDistance.NOWHERE_NEAR, result);
	}

	@Test
	public void getDistanceToBank_shouldReturnCorrectDistance()
	{
		BankLocation bank = BankLocation.VARROCK_WEST;
		WorldPoint point = bank.centerPoint;

		BankDistance result = BankDistanceFinder.getDistanceToBank(bank, point);

		assertEquals(BankDistance.INSIDE, result);
	}

	@Test
	public void getDistanceToBank_shouldReturnFarDistance()
	{
		BankLocation bank = BankLocation.VARROCK_WEST;

		WorldPoint point = new WorldPoint(
			bank.centerPoint.getX() + 100,
			bank.centerPoint.getY() + 100,
			bank.centerPoint.getPlane()
		);

		BankDistance result = BankDistanceFinder.getDistanceToBank(bank, point);

		assertEquals(BankDistance.NOWHERE_NEAR, result);
	}
}