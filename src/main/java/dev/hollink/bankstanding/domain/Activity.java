package dev.hollink.bankstanding.domain;

import java.time.Duration;
import java.time.Instant;
import lombok.Value;

@Value
public class Activity<T>
{
	T value;
	Instant time;

	public ActivityWindow within(Duration gracePeriod)
	{
		return (since) -> Duration.between(time, since).compareTo(gracePeriod) < 0;
	}

	public interface ActivityWindow
	{
		boolean isActiveAt(Instant time);
	}
}
