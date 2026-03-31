package dev.hollink.bankstanding.overlay.debug;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.domain.Activity;
import dev.hollink.bankstanding.domain.PlayerState;
import dev.hollink.bankstanding.overlay.OverlayHelper;
import dev.hollink.bankstanding.state.level.ExperienceManager;
import dev.hollink.bankstanding.state.player.PlayerStateManager;
import dev.hollink.bankstanding.utils.BankDistanceFinder;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.ui.overlay.OverlayPanel;

import static dev.hollink.bankstanding.config.ExpRateConstants.TIME_BETWEEN_DROPS;
import static dev.hollink.bankstanding.config.TimeConstants.GRACE_PERIOD_CHATTING;
import static dev.hollink.bankstanding.config.TimeConstants.GRACE_PERIOD_GRINDING;
import static dev.hollink.bankstanding.config.TimeConstants.GRACE_PERIOD_MOVEMENT;
import static dev.hollink.bankstanding.config.TimeConstants.TIME_TILL_INITIAL_EXP;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PlayerStateDebugOverlay extends OverlayPanel implements OverlayHelper
{
	private final Client client;
	private final BankstandingConfig config;

	private final PlayerStateManager stateManager;
	private final ExperienceManager xpManager;

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.displayDebugPanel())
		{
			return super.render(graphics);
		}

		setPanelWidth(160, panelComponent);
		addPanelPadding(panelComponent);
		addTitle("Bankstanding Debugger", panelComponent);
		addText("State:", enumToString(stateManager.getCurrentPlayerState().getActivity().name()), panelComponent);
		addText("Time left in state:", String.format("%ds", timeTillStateUpdates()), panelComponent);
		addClosestBankInfo();
		addText("Xp in:", String.format("%ds", timeTillExpDrop()), panelComponent);

		return super.render(graphics);
	}

	private void addClosestBankInfo()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			return;
		}
		BankDistanceFinder.getCLosestBank(player.getWorldLocation())
			.ifPresentOrElse(bank -> {
				addText("Bank:", bank.getDisplayName(), panelComponent);
				addText("Distance:", enumToString(BankDistanceFinder.getDistanceToBank(bank, player.getWorldLocation()).name()), panelComponent);
			}, () -> {
				addText("Bank:", "-", panelComponent);
				addText("Distance:", "-", panelComponent);
			});
	}

	public long timeTillStateUpdates()
	{
		PlayerState currentPlayerState = stateManager.getCurrentPlayerState();
		switch (currentPlayerState.getActivity())
		{
			case CHATTING:
				return secondsSince(stateManager.getLastChatMessage(), GRACE_PERIOD_CHATTING);
			case LOAFING:
				return secondsSince(stateManager.getLastMovement(), GRACE_PERIOD_MOVEMENT);
			case GRINDING:
				return secondsSince(stateManager.getLastExperienceDrop(), GRACE_PERIOD_GRINDING);
		}
		return 0;
	}


	public long timeTillExpDrop()
	{
		long secondsTillInitialDrop = Duration.between(Instant.now(), xpManager.getLastStateChange().plus(TIME_TILL_INITIAL_EXP)).toSeconds();
		if (secondsTillInitialDrop > 0)
		{
			return secondsTillInitialDrop;
		}
		else
		{
			long secondsTillNextExpDrop = Duration.between(Instant.now(), xpManager.getLastExpDrop().plus(TIME_BETWEEN_DROPS)).toSeconds();
			return Math.max(secondsTillNextExpDrop, 0);
		}
	}

	private long secondsSince(Activity<?> activity, Duration gracePeriod)
	{
		return Duration.between(Instant.now(), activity.getTime().plus(gracePeriod)).toSeconds();
	}

	private String enumToString(String name)
	{
		return Arrays.stream(name.split("_"))
			.map(word -> word.charAt(0) + word.substring(1).toLowerCase())
			.collect(Collectors.joining(" "));
	}
}
