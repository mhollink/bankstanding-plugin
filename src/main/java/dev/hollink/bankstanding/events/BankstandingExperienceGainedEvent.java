package dev.hollink.bankstanding.events;

import lombok.Value;

@Value
public class BankstandingExperienceGainedEvent implements BankstandingEvent
{
	double experienceGained;
	boolean leveledUp;
}
