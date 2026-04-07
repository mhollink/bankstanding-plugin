package dev.hollink.bankstanding.state.bankstats;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.BankstandingPlugin;
import dev.hollink.bankstanding.config.BankDistance;
import dev.hollink.bankstanding.config.BankLocation;
import dev.hollink.bankstanding.domain.BankStats;
import dev.hollink.bankstanding.panel.BankstandingPanel;
import dev.hollink.bankstanding.utils.BankDistanceFinder;
import java.util.Optional;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BankStatsManagerTest
{
	private Client mockClient;
	private BankstandingPlugin mockPlugin;
	private BankstandingConfig mockConfig;
	private BankStatsManager bankStatsManager;
	private Player mockPlayer;
	private BankstandingPanel mockPanel;

	@Before
	public void setUp()
	{
		mockClient = mock(Client.class);
		mockPlugin = mock(BankstandingPlugin.class);
		mockConfig = mock(BankstandingConfig.class);
		mockPlayer = mock(Player.class);
		when(mockClient.getLocalPlayer()).thenReturn(mockPlayer);

		when(mockConfig.enableTracker()).thenReturn(true);

		mockPanel = mock(BankstandingPanel.class);
		when(mockPlugin.getBankStatsPanel()).thenReturn(mockPanel);

		bankStatsManager = new BankStatsManager(mockClient, mockPlugin, null, mockConfig, null); // ignore Gson/configManager
	}

	@Test
	public void onTick_shouldAddTickAndVisit_whenPlayerInsideBank()
	{
		BankLocation bankLocation = BankLocation.LUMBRIDGE;
		WorldPoint playerLocation = BankLocation.LUMBRIDGE.getCenterPoint();
		when(mockPlayer.getWorldLocation()).thenReturn(playerLocation);

		when(mockPlayer.getAnimation()).thenReturn(-1); // idle

		bankStatsManager.onTick();

		BankStats session = bankStatsManager.getSessionStats().get(bankLocation);
		BankStats allTime = bankStatsManager.getAllTimeStats().get(bankLocation);

		assertNotNull(session);
		assertNotNull(allTime);

		assertEquals(1, session.getTotalTicks());
		assertEquals(1, session.getVisits());
		assertEquals(1, allTime.getTotalTicks());
		assertEquals(1, allTime.getVisits());
	}

	@Test
	public void onTick_shouldCountActiveTick_whenAnimationIsNotIdle()
	{
		BankLocation bankLocation = BankLocation.LUMBRIDGE;
		WorldPoint playerLocation = BankLocation.LUMBRIDGE.getCenterPoint();
		when(mockPlayer.getWorldLocation()).thenReturn(playerLocation);

		when(mockPlayer.getAnimation()).thenReturn(1234); // active

		bankStatsManager.onTick();

		BankStats session = bankStatsManager.getSessionStats().get(bankLocation);
		assertEquals(1, session.getTotalTicks());
		assertEquals(0, session.getIdleTicks());
		assertEquals(1, session.getActiveTicks());
	}

	@Test
	public void onTick_shouldSkipTick_whenBankIsOpen_andExcludeBankOpenTrue()
	{
		when(mockConfig.excludeBankOpen()).thenReturn(true);
		Widget bankWidget = mock(Widget.class);
		when(bankWidget.isHidden()).thenReturn(false);
		when(mockClient.getWidget(InterfaceID.BANKMAIN, 0)).thenReturn(bankWidget);

		bankStatsManager.onTick();

		assertTrue(bankStatsManager.getSessionStats().isEmpty());
		assertTrue(bankStatsManager.getAllTimeStats().isEmpty());
	}
}