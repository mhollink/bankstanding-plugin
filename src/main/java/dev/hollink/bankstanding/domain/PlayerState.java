package dev.hollink.bankstanding.domain;

import dev.hollink.bankstanding.config.ActivityState;
import java.time.Instant;
import lombok.Value;

@Value
public class PlayerState
{
	Instant since;
	ActivityState activity;
}