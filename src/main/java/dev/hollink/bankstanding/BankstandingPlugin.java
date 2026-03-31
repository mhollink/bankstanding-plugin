package dev.hollink.bankstanding;

import com.google.inject.Provides;
import dev.hollink.bankstanding.overlay.BankstandingOverlayManager;
import dev.hollink.bankstanding.overlay.ExperienceOverlayStateManager;
import dev.hollink.bankstanding.panel.BankstandingPanel;
import dev.hollink.bankstanding.state.ChatCommandHandler;
import dev.hollink.bankstanding.state.bankstats.BankStatsManager;
import dev.hollink.bankstanding.state.level.ExperienceManager;
import dev.hollink.bankstanding.state.level.LevelUpHandler;
import dev.hollink.bankstanding.state.player.PlayerStateManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
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

	private Player currentPlayer = null;

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
		List<GameState> resettableStates = List.of(GameState.LOGIN_SCREEN, GameState.HOPPING);
		if (resettableStates.contains(gameStateChanged.getGameState()))
		{
			log.debug("Resetting state due to sign-out/hopping");
			currentPlayer = null;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (isPlaying())
		{
			playerStateManager.checkForStateChanges();
			experienceManager.onTick();
			bankStatsManager.onTick();
		}
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

	private boolean isPlaying()
	{
		Player player = client.getLocalPlayer();
		if (player == null)
		{
			// There is no player object,
			// so we are not actively in-game
			return false;
		}

		if (currentPlayer == null)
		{
			// There is a player object, but no current player;
			// Wake-up all components and assign the current player.
			startBankstanding(player);
			return true;
		}

		if (Objects.equals(player.getName(), currentPlayer.getName()))
		{
			// The client is still using the same active player;
			// Continue bankstanding...
			return true;
		}
		else
		{
			// There was a switch within the active player;
			// Restart all the components and continue bankstanding...
			startBankstanding(player);
			return true;
		}
	}

	private void startBankstanding(Player player)
	{
		currentPlayer = player;
		log.debug("Player {} logged in, start bankstanding experience tracking", player.getName());

		playerStateManager.startUp();
		experienceManager.startUp();
		progressOverlayStateManager.startUp();
		bankStatsManager.startUp();
	}
}
