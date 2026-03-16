package dev.hollink.bankstanding.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityState
{
	AFK(1.2),
	CHATTING(1.1),
	LOAFING(0.3),
	GRINDING(0);

	public final double expMultiplier;
}