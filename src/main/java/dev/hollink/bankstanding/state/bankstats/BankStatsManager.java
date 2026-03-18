package dev.hollink.bankstanding.state.bankstats;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.BankstandingPlugin;
import dev.hollink.bankstanding.config.BankDistance;
import dev.hollink.bankstanding.config.BankLocation;
import dev.hollink.bankstanding.domain.BankStats;
import dev.hollink.bankstanding.utils.BankDistanceFinder;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;

@Slf4j
@Singleton
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BankStatsManager
{
	private static final int SAVE_INTERVAL_TICKS = 100; // every 1 minute.

	private final Client client;
	private final BankstandingPlugin plugin;
	private final ConfigManager configManager;
	private final BankstandingConfig config;
	private final Gson gson;

	@Getter
	private final Map<BankLocation, BankStats> sessionStats = new LinkedHashMap<>();
	@Getter
	private final Map<BankLocation, BankStats> allTimeStats = new LinkedHashMap<>();

	private BankLocation currentLocation;
	private int ticksSinceLastSave = 0;
	private int ticksSinceLastPanelRefresh = 0;

	public void startUp()
	{
		restoreSavedData();
	}

	public void onTick()
	{
		if (config.excludeBankOpen() && bankIsOpen()) {
			return;
		}

		getBankLocation().ifPresentOrElse(this::addTickToBankLocation, () -> currentLocation = null);
	}

	private boolean bankIsOpen()
	{
		Widget bankWidget = client.getWidget(InterfaceID.BANKMAIN, 0);
		return bankWidget != null && !bankWidget.isHidden();
	}

	private void addTickToBankLocation(BankLocation bankLocation)
	{
		boolean idle = client.getLocalPlayer().getAnimation() == -1;

		BankStats session = sessionStats.computeIfAbsent(bankLocation, k -> new BankStats());
		BankStats allTime = allTimeStats.computeIfAbsent(bankLocation, k -> new BankStats());

		session.addTick(idle);
		allTime.addTick(idle);

		// A new visit starts when we arrive at this bank (different from last tick)
		if (!bankLocation.equals(currentLocation))
		{
			session.addVisit();
			allTime.addVisit();
			currentLocation = bankLocation;
		}

		if (++ticksSinceLastSave >= SAVE_INTERVAL_TICKS)
		{
			saveData();
			ticksSinceLastSave = 0;
		}

		if (++ticksSinceLastPanelRefresh >= 2)
		{
			SwingUtilities.invokeLater(plugin.getBankStatsPanel()::refresh);
			ticksSinceLastPanelRefresh = 0;
		}
	}

	private void restoreSavedData()
	{
		String data = configManager.getRSProfileConfiguration(BankstandingConfig.CONFIG_GROUP, "bankStats");
		if (data == null || data.isEmpty())
		{
			return;
		}
		try
		{
			Type type = getSaveDataType();
			Map<BankLocation, BankStats> loaded = gson.fromJson(data, type);
			if (loaded != null)
			{
				allTimeStats.clear();
				allTimeStats.putAll(loaded);
				log.debug("Loaded saved data... {}", loaded);
			}
		}
		catch (Exception ex)
		{
			log.warn("Failed to load bankstanding stats", ex);
		}
	}

	private static Type getSaveDataType()
	{
		TypeToken<Map<BankLocation, BankStats>> typeToken = new TypeToken<>()
		{
		};
		return typeToken.getType();
	}

	public void saveData()
	{
		String data = gson.toJson(allTimeStats);
		log.debug("Saving data... {}", data);
		configManager.setRSProfileConfiguration(BankstandingConfig.CONFIG_GROUP, "bankStats", data);
	}

	public Optional<BankLocation> getBankLocation()
	{
		WorldPoint location = client.getLocalPlayer().getWorldLocation();
		return BankDistanceFinder.getCLosestBank(location)
			.filter(bank -> BankDistanceFinder.getDistanceToBank(bank, location).equals(BankDistance.INSIDE));
	}
}
