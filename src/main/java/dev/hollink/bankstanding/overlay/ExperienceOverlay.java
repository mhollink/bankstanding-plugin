package dev.hollink.bankstanding.overlay;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.state.level.ExperienceManager;
import dev.hollink.bankstanding.utils.ExperienceFormatter;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.OverlayPanel;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ExperienceOverlay extends OverlayPanel implements OverlayHelper
{
	private final ExperienceManager experienceManager;
	private final ExperienceOverlayStateManager stateManager;
	private final BankstandingConfig config;

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showBankstandingExperienceOverlay())
		{
			return super.render(graphics);
		}

		// Don't render if player is too far away from a bank region.
		if (experienceManager.getBankDistance().ordinal() > config.panelHideDistance().ordinal())
		{
			return super.render(graphics);
		}

		if (stateManager.hasRecentlyGainedExp())
		{
			addExperienceOverlay();
		}

		return super.render(graphics);
	}


	private void addExperienceOverlay()
	{
		setPanelWidth(160, panelComponent);
		addPanelPadding(panelComponent);
		addText("Bankstanding", String.valueOf(stateManager.getCurrentLvl()), panelComponent);
		addLabelledText("Current xp:", stateManager.getCurrentXp(), panelComponent);
		addLabelledText("Xp to level:", stateManager.getXpToLevel(), panelComponent);
		addLineBreak(panelComponent);
		addProgressBar(stateManager.getProgress(), panelComponent);
	}
}
