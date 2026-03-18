package dev.hollink.bankstanding;

import com.google.inject.Provides;
import dev.hollink.bankstanding.overlay.ExperienceOverlay;
import dev.hollink.bankstanding.overlay.BankstandingOverlayManager;
import dev.hollink.bankstanding.overlay.ExperienceOverlayStateManager;
import dev.hollink.bankstanding.panel.BankstandingPanel;
import dev.hollink.bankstanding.state.bankstats.BankStatsManager;
import dev.hollink.bankstanding.state.level.ExperienceManager;
import dev.hollink.bankstanding.state.ChatCommandHandler;
import dev.hollink.bankstanding.state.level.LevelUpHandler;
import dev.hollink.bankstanding.state.player.PlayerStateManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;

@Slf4j
@PluginDescriptor(
	name = "Bankstanding",
	description = "Gain (fake) experience with doing absolutely nothing",
	tags = {"bankstanding", "xp", "experience", "nothing", "afk"}
)
public class BankstandingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private PlayerStateManager playerStateManager;

	@Inject
	private ExperienceManager experienceManager;

	@Inject
	private BankstandingOverlayManager overlayManager;

	@Inject
	private ExperienceOverlayStateManager progressOverlayStateManager;

	@Inject
	private ChatCommandManager chatCommandManager;

	@Inject
	private ChatCommandHandler chatCommandHandler;

	@Inject
	private BankStatsManager bankStatsManager;

	@Inject
	private LevelUpHandler levelUpHandler;

	@Getter
	@Inject
	private BankstandingPanel bankStatsPanel;

	@Inject
	private ClientToolbar clientToolbar;
	private NavigationButton navButton;

	@Override
	protected void startUp()
	{
		progressOverlayStateManager.init();
		experienceManager.init();
		overlayManager.init();
		levelUpHandler.init();

		chatCommandManager.registerCommand("!lvl", chatCommandHandler::handleLevelCommand);
		clientToolbar.addNavigation(navButton = buildNavButton());

	}

	@Override
	protected void shutDown()
	{
		bankStatsManager.saveData();

		overlayManager.destroy();
		experienceManager.destroy();
		levelUpHandler.destroy();
		progressOverlayStateManager.destroy();

		chatCommandManager.unregisterCommand("!lvl");
		clientToolbar.removeNavigation(navButton);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			log.debug("Player logged in, start bankstanding experience tracking");
			playerStateManager.startUp();
			experienceManager.startUp();
			progressOverlayStateManager.startUp();
			bankStatsManager.startUp();
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (client.getLocalPlayer() == null)
		{
			return;
		}

		playerStateManager.checkForStateChanges();
		experienceManager.onTick();
		bankStatsManager.onTick();
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		playerStateManager.onChatMessage(event);
	}

	@Provides
	BankstandingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BankstandingConfig.class);
	}


	private NavigationButton buildNavButton()
	{
		BufferedImage icon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = icon.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// Gold coin-ish icon background
		g.setColor(new Color(180, 150, 60));
		g.fillOval(1, 1, 14, 14);
		g.setColor(new Color(220, 190, 100));
		g.fillOval(3, 3, 10, 10);
		// "B" letter
		g.setColor(new Color(80, 60, 20));
		g.setFont(new Font("Dialog", Font.BOLD, 9));
		g.drawString("B", 4, 12);
		g.dispose();

		return NavigationButton.builder()
			.tooltip("Bankstanding Tracker")
			.icon(icon)
			.priority(6)
			.panel(bankStatsPanel)
			.build();
	}
}
