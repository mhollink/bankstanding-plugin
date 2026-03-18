package dev.hollink.bankstanding.overlay;

import dev.hollink.bankstanding.overlay.debug.BankLocationDebugOverlay;
import dev.hollink.bankstanding.overlay.debug.PlayerStateDebugOverlay;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankstandingOverlayManager
{
	private final OverlayManager overlayManager;

	private final ExperienceOverlay progressOverlay;
	private final PlayerStateDebugOverlay debugInfoOverlay;
	private final BankLocationDebugOverlay debugBankOverlay;
	private final ConfettiOverlay confettiOverlay;
	private final BankStatsOverlay bankStatsOverlay;

	public void init()
	{
		overlayManager.add(progressOverlay);
		overlayManager.add(debugInfoOverlay);
		overlayManager.add(debugBankOverlay);
		overlayManager.add(confettiOverlay);
		overlayManager.add(bankStatsOverlay);
	}

	public void destroy()
	{
		overlayManager.remove(confettiOverlay);
		overlayManager.remove(progressOverlay);
		overlayManager.remove(debugInfoOverlay);
		overlayManager.remove(debugBankOverlay);
		overlayManager.remove(bankStatsOverlay);
	}
}
