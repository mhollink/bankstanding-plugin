package dev.hollink.bankstanding.state.level;

import dev.hollink.bankstanding.domain.BankstandingLevel;
import dev.hollink.bankstanding.overlay.ExperienceOverlayStateManager;
import dev.hollink.bankstanding.state.CommandHandler;
import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Experience;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;

import static dev.hollink.bankstanding.BankstandingConfig.CONFIG_GROUP;
import static dev.hollink.bankstanding.BankstandingConfig.CURRENT_EXPERIENCE_CONFIG_KEY;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LevelSetCommandHandler implements CommandHandler
{
	private final ConfigManager configManager;
	private final ExperienceManager xpManager;
	private final ExperienceOverlayStateManager overlay;


	public void handleCommand(ChatMessage chatMessage, String message)
		throws InvalidValueArgumentException, InvalidTypeArgumentException
	{
		String[] args = message.split(" ");

		log.debug("Received set level command: {}", Arrays.toString(args));
		if (args.length != 3)
		{
			return;
		}

		String type = args[1].toLowerCase();
		int exp = getExperience(type, args[2]);

		setExperience(exp);
	}

	private int getExperience(String type, String args)
	{
		switch (type)
		{
			case "level":
				return Experience.getXpForLevel(getIntValue(args));
			case "exp":
				return getIntValue(args);
			default:
				throw new InvalidTypeArgumentException(type);
		}
	}

	private int getIntValue(String arg)
	{
		try
		{
			return Integer.parseInt(arg);
		}
		catch (NumberFormatException e)
		{
			throw new InvalidValueArgumentException(arg);
		}
	}

	private void setExperience(int experience)
	{
		log.debug("Setting bankstanding experience to {}", experience);
		xpManager.setBankstanding(new BankstandingLevel(experience));
		overlay.refreshExperience();
		configManager.setRSProfileConfiguration(
			CONFIG_GROUP,
			CURRENT_EXPERIENCE_CONFIG_KEY,
			String.valueOf(experience)
		);
	}

	@RequiredArgsConstructor
	public static class InvalidValueArgumentException extends IllegalArgumentException
	{
		final String value;

		@Override
		public String getMessage()
		{
			return String.format("Invalid setLevel argument, value must be number but \"%s\" was given.", value);
		}
	}

	@RequiredArgsConstructor
	public static class InvalidTypeArgumentException extends IllegalArgumentException
	{
		final String type;

		@Override
		public String getMessage()
		{
			return String.format("Invalid setLevel argument, type must be one of [level, exp] but \"%s\" was given.", type);
		}
	}

}
