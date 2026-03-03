package dev.hollink.bankstanding;

import com.google.inject.Provides;
import dev.hollink.bankstanding.overlay.BankstandingLevelProgressOverlay;
import dev.hollink.bankstanding.overlay.BankstandingOverlayManager;
import dev.hollink.bankstanding.state.BankstandingExperienceManager;
import dev.hollink.bankstanding.state.ChatCommandHandler;
import dev.hollink.bankstanding.state.LevelUpHandler;
import dev.hollink.bankstanding.state.PlayerStateManager;
import javax.inject.Inject;
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
	private BankstandingExperienceManager experienceManager;

	@Inject
	private BankstandingOverlayManager overlayManager;

	@Inject
	private BankstandingLevelProgressOverlay progressOverlay;

	@Inject
	private ChatCommandManager chatCommandManager;

	@Inject
	private ChatCommandHandler chatCommandHandler;

	@Inject
	private LevelUpHandler levelUpHandler;

	@Override
	protected void startUp()
	{
		experienceManager.init();
		overlayManager.init();
		levelUpHandler.init();

		chatCommandManager.registerCommand("!lvl", chatCommandHandler::handleLevelCommand);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.destroy();
		experienceManager.destroy();
		levelUpHandler.destroy();

		chatCommandManager.unregisterCommand("!lvl");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			log.debug("Player logged in, start bankstanding experience tracking");
			playerStateManager.startUp();
			experienceManager.startUp();
			progressOverlay.startUp();
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
}
