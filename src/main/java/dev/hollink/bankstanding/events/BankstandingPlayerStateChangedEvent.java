package dev.hollink.bankstanding.events;

import dev.hollink.bankstanding.domain.PlayerState;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@Value
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BankstandingPlayerStateChangedEvent implements BankstandingEvent
{
	PlayerState oldState;
	PlayerState newState;
}
