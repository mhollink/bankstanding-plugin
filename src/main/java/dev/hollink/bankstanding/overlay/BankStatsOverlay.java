package dev.hollink.bankstanding.overlay;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.config.BankLocation;
import dev.hollink.bankstanding.domain.BankStats;
import dev.hollink.bankstanding.state.bankstats.BankStatsManager;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.OverlayPanel;

import static dev.hollink.bankstanding.domain.BankStats.formatTicksToTime;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankStatsOverlay extends OverlayPanel implements OverlayHelper
{
	private final BankStatsManager bankStatsManager;
	private final BankstandingConfig config;

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showBankStatsOverlay())
		{
			return super.render(graphics);
		}

		Optional<BankLocation> currentBank = bankStatsManager.getBankLocation();
		if (currentBank.isPresent())
		{
			BankLocation bankLocation = currentBank.get();
			BankStats session = bankStatsManager.getSessionStats().getOrDefault(bankLocation, new BankStats());

			addBankingData(bankLocation, session);
		}

		return super.render(graphics);
	}

	private void addBankingData(BankLocation bankLocation, BankStats session)
	{
		setPanelWidth(160, panelComponent);
		addTitle(bankLocation.name(), panelComponent);
		addLineBreak(panelComponent);
		addLabelledText("Session", formatTicksToTime(session.getTotalTicks()), panelComponent);
		addLabelledText("Idle", formatTicksToTime(session.getIdleTicks()) + "  (" + session.idlePercent() + "%)", panelComponent);
		addLabelledText("Active", formatTicksToTime(session.getActiveTicks()) + "  (" + session.activePercent() + "%)", panelComponent);
		addLabelledText("Visits", String.valueOf(session.getVisits()), panelComponent);
	}
}
