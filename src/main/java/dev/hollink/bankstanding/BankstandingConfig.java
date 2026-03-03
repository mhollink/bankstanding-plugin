package dev.hollink.bankstanding;

import static dev.hollink.bankstanding.BankstandingConfig.CONFIG_GROUP;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(CONFIG_GROUP)
public interface BankstandingConfig extends Config
{
	String CONFIG_GROUP = "Bankstanding";

	String CURRENT_EXPERIENCE_CONFIG_KEY = "current_experience";


	@ConfigItem(
		keyName = "bankHighlight",
		name = "Highlight bank tiles",
		description = "Draw a tilemaker on the tiles which are configured in as 'Bank'"
	)
	default boolean bankHighlight()
	{
		return false;
	}

	@ConfigItem(
		keyName = "debugPanel",
		name = "Display debug panel",
		description = "Add a secondary panel which shows the active player state and distance towards the closest bank"
	)
	default boolean displayDebugPanel()
	{
		return false;
	}
}
