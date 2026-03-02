package dev.hollink.bankstanding;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class BankstandingPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(BankstandingPlugin.class);
		RuneLite.main(args);
	}
}