package dev.hollink.bankstanding.state.player;

import dev.hollink.bankstanding.config.ActivityState;
import static dev.hollink.bankstanding.config.TimeConstants.GRACE_PERIOD_CHATTING;
import static dev.hollink.bankstanding.config.TimeConstants.GRACE_PERIOD_GRINDING;
import static dev.hollink.bankstanding.config.TimeConstants.GRACE_PERIOD_MOVEMENT;
import dev.hollink.bankstanding.domain.Activity;
import dev.hollink.bankstanding.domain.PlayerState;
import dev.hollink.bankstanding.events.BankstandingEvent;
import dev.hollink.bankstanding.events.BankstandingEventBus;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import static net.runelite.api.ChatMessageType.CLAN_CHAT;
import static net.runelite.api.ChatMessageType.PRIVATECHATOUT;
import static net.runelite.api.ChatMessageType.PUBLICCHAT;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerStateManager
{
	private final Client client;
	private final BankstandingEventBus events;

	@Getter
	private PlayerState currentPlayerState;

	@Getter
	private Activity<WorldPoint> lastMovement;

	@Getter
	private Activity<Long> lastExperienceDrop;

	@Getter
	private Activity<Void> lastChatMessage;

	public void startUp()
	{
		currentPlayerState = new PlayerState(Instant.now(), ActivityState.NULL);

		lastExperienceDrop = new Activity<>(client.getOverallExperience(), Instant.EPOCH);
		lastMovement = new Activity<>(client.getLocalPlayer().getWorldLocation(), Instant.EPOCH);
		lastChatMessage = new Activity<>(null, Instant.EPOCH);

		log.debug("PlayerStateManager started");
	}

	public void checkForStateChanges()
	{
		updateActivityTimers();

		ActivityState activity = determineCurrentActivity();
		if (currentPlayerState.getActivity() != activity)
		{
			currentPlayerState = switchActivity(activity);
		}
	}

	public void onChatMessage(ChatMessage event)
	{
		final String playerName = client.getLocalPlayer().getName();
		final List<ChatMessageType> messageTypes = List.of(PUBLICCHAT, PRIVATECHATOUT, CLAN_CHAT);

		boolean isChatMessage = messageTypes.contains(event.getType());
		boolean isFromPlayer = event.getName() != null && event.getName().equals(playerName);
		if (isChatMessage && isFromPlayer)
		{
			log.debug("Player has sent a chat message!");
			lastChatMessage = new Activity<>(null, Instant.now());
		}
	}

	private void updateExpTimer()
	{
		long currentXp = client.getOverallExperience();
		long gained = currentXp - lastExperienceDrop.getValue();
		if (gained > 0)
		{
			log.debug("Player has gained {} experience!", gained);
			lastExperienceDrop = new Activity<>(currentXp, Instant.now());
		}
	}

	private void updateMovementTimer()
	{
		WorldPoint location = client.getLocalPlayer().getWorldLocation();
		int distanceMoved = location.distanceTo(lastMovement.getValue());
		if (distanceMoved > 5)
		{
			log.debug("Player has to moved {} tiles!", distanceMoved);
			lastMovement = new Activity<>(location, Instant.now());
		}
	}

	private void updateActivityTimers()
	{
		updateExpTimer();
		updateMovementTimer();
	}

	private ActivityState determineCurrentActivity()
	{
		Instant now = Instant.now();
		if (lastExperienceDrop.within(GRACE_PERIOD_GRINDING).isActiveAt(now))
		{
			return ActivityState.GRINDING;
		}

		if (lastMovement.within(GRACE_PERIOD_MOVEMENT).isActiveAt(now))
		{
			return ActivityState.LOAFING;
		}

		if (lastChatMessage.within(GRACE_PERIOD_CHATTING).isActiveAt(now))
		{
			return ActivityState.CHATTING;
		}

		return ActivityState.AFK;
	}


	private PlayerState switchActivity(ActivityState newState)
	{
		PlayerState newPlayerState = new PlayerState(Instant.now(), newState);

		events.publish(
			BankstandingEvent.stateChanged()
				.oldState(currentPlayerState)
				.newState(newPlayerState)
				.build()
		);

		log.debug("Activity changed to: {}", newPlayerState.getActivity());
		return newPlayerState;
	}
}
