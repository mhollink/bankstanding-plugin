package dev.hollink.bankstanding.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityState
{
	AFK(1.20),
	CHATTING(0.60),
	LOAFING(0.30),
	GRINDING(0.01);

	final double expMultiplier;
}