package dev.hollink.bankstanding.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BankStatsTest
{
	@Test
	public void addTick_shouldIncreaseTotalAndIdleTicks_whenIdle()
	{
		BankStats stats = new BankStats();

		stats.addTick(true);

		assertEquals(1, stats.getTotalTicks());
		assertEquals(1, stats.getIdleTicks());
		assertEquals(0, stats.getActiveTicks());
	}

	@Test
	public void addTick_shouldIncreaseTotalAndActiveTicks_whenNotIdle()
	{
		BankStats stats = new BankStats();

		stats.addTick(false);

		assertEquals(1, stats.getTotalTicks());
		assertEquals(0, stats.getIdleTicks());
		assertEquals(1, stats.getActiveTicks());
	}

	@Test
	public void addVisit_shouldIncreaseVisits()
	{
		BankStats stats = new BankStats();

		stats.addVisit();
		stats.addVisit();

		assertEquals(2, stats.getVisits());
	}

	@Test
	public void merge_shouldCombineAllFields()
	{
		BankStats a = new BankStats();
		a.addTick(true);
		a.addTick(false);
		a.addVisit();

		BankStats b = new BankStats();
		b.addTick(true);
		b.addVisit();
		b.addVisit();

		a.merge(b);

		assertEquals(3, a.getTotalTicks());
		assertEquals(2, a.getIdleTicks());
		assertEquals(1, a.getActiveTicks());
		assertEquals(3, a.getVisits());
	}

	@Test
	public void idlePercent_shouldReturnZero_whenNoTicks()
	{
		BankStats stats = new BankStats();

		assertEquals(0, stats.idlePercent());
	}

	@Test
	public void activePercent_shouldReturnZero_whenNoTicks()
	{
		BankStats stats = new BankStats();

		assertEquals(0, stats.activePercent());
	}

	@Test
	public void idlePercent_shouldCalculateCorrectly()
	{
		BankStats stats = new BankStats();
		stats.addTick(true);
		stats.addTick(true);
		stats.addTick(false);

		assertEquals(66, stats.idlePercent());
	}

	@Test
	public void activePercent_shouldCalculateCorrectly()
	{
		BankStats stats = new BankStats();
		stats.addTick(true);
		stats.addTick(false);
		stats.addTick(false);

		assertEquals(66, stats.activePercent());
	}

	@Test
	public void formatTicksToTime_shouldReturnSecondsOnly_whenUnderOneMinute()
	{
		String result = BankStats.formatTicksToTime(1);

		assertEquals("1s", result);
	}

	@Test
	public void formatTicksToTime_shouldReturnMinutesAndSeconds()
	{
		long ticks = 100;

		String result = BankStats.formatTicksToTime(ticks);

		assertEquals("1m 00s", result);
	}

	@Test
	public void formatTicksToTime_shouldReturnHoursMinutesAndSeconds()
	{
		long ticks = 6000;

		String result = BankStats.formatTicksToTime(ticks);

		assertEquals("1h 00m 00s", result);
	}
}