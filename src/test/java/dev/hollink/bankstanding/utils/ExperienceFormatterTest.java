package dev.hollink.bankstanding.utils;

import dev.hollink.bankstanding.config.ExperienceNotation;
import org.junit.Test;

import static dev.hollink.bankstanding.utils.ExperienceFormatter.valueOfExp;
import static org.junit.Assert.assertEquals;

public class ExperienceFormatterTest
{
	@Test
	public void shouldReturnFullValue_whenFullNotation()
	{
		assertEquals("123456", valueOfExp(123456, ExperienceNotation.FULL));
	}

	@Test
	public void shouldFormatK_withoutDecimal_whenExact()
	{
		assertEquals("2K", valueOfExp(2000, ExperienceNotation.K));
	}

	@Test
	public void shouldFormatK_withDecimal_whenNotExact()
	{
		assertEquals("2.5K", valueOfExp(2500, ExperienceNotation.K));
	}

	@Test
	public void shouldReturnRawValue_whenBelowKThreshold()
	{
		assertEquals("999", valueOfExp(999, ExperienceNotation.K));
	}

	@Test
	public void shouldFormatM_withoutDecimal_whenExact()
	{
		assertEquals("2M", valueOfExp(2_000_000, ExperienceNotation.M));
	}

	@Test
	public void shouldFormatM_withDecimal_whenNotExact()
	{
		assertEquals("2.5M", valueOfExp(2_500_000, ExperienceNotation.M));
	}

	@Test
	public void shouldReturnRawValue_whenBelowMThreshold()
	{
		assertEquals("999999", valueOfExp(999_999, ExperienceNotation.M));
	}

	@Test
	public void shouldReturnRaw_whenAutoBelow100K()
	{
		assertEquals("99999", valueOfExp(99_999, ExperienceNotation.AUTO));
	}

	@Test
	public void shouldUseK_whenAutoAt100K()
	{
		assertEquals("100K", valueOfExp(100_000, ExperienceNotation.AUTO));
	}

	@Test
	public void shouldUseK_whenAutoMidRange()
	{
		assertEquals("250K", valueOfExp(250_000, ExperienceNotation.AUTO));
	}

	@Test
	public void shouldUseM_whenAutoAt10M()
	{
		assertEquals("10M", valueOfExp(10_000_000, ExperienceNotation.AUTO));
	}

	@Test
	public void shouldUseM_withDecimal_whenAutoAbove10M()
	{
		assertEquals("12.5M", valueOfExp(12_500_000, ExperienceNotation.AUTO));
	}

	@Test
	public void shouldHandleZero()
	{
		assertEquals("0", valueOfExp(0, ExperienceNotation.AUTO));
	}
}