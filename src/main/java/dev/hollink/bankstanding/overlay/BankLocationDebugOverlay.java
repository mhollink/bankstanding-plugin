package dev.hollink.bankstanding.overlay;

import dev.hollink.bankstanding.BankstandingConfig;
import dev.hollink.bankstanding.config.BankLocation;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
@Singleton
public class BankLocationDebugOverlay extends Overlay
{
	private static final Color IN_BANK = new Color(181, 255, 105, 200);
	private static final Color NOT_IN_BANK = new Color(255, 181, 105, 200);

	private final Client client;
	private final BankstandingConfig config;

	@Inject
	public BankLocationDebugOverlay(Client client, BankstandingConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.bankHighlight())
		{
			return null;
		}

		for (BankLocation bank : BankLocation.values())
		{
			renderBankArea(graphics, bank);
		}

		return null;
	}

	private void renderBankArea(Graphics2D graphics, BankLocation bank)
	{
		WorldPoint player = client.getLocalPlayer().getWorldLocation();
		if (bank.centerPoint.getPlane() != player.getPlane())
		{
			return;
		}

		WorldArea bankArea = fromCenter(bank.centerPoint, bank.size + 2);
		Color overlayColor = player.isInArea(bankArea) ? IN_BANK : NOT_IN_BANK;

		renderWorldAreaOutline(graphics, bankArea, overlayColor);
		renderTile(graphics, bank.centerPoint, overlayColor);
	}

	public static WorldArea fromCenter(WorldPoint center, int size)
	{
		return new WorldArea(center.getX() - size, center.getY() - size, size * 2, size * 2, center.getPlane());
	}

	private void renderWorldAreaOutline(Graphics2D graphics, WorldArea area, Color color)
	{
		int x1 = area.getX();
		int y1 = area.getY();
		int x2 = x1 + area.getWidth();
		int y2 = y1 + area.getHeight();
		int plane = area.getPlane();

		WorldPoint sw = new WorldPoint(x1, y1, plane);
		WorldPoint nw = new WorldPoint(x1, y2, plane);
		WorldPoint ne = new WorldPoint(x2, y2, plane);
		WorldPoint se = new WorldPoint(x2, y1, plane);

		LocalPoint lsw = LocalPoint.fromWorld(client, sw);
		LocalPoint lnw = LocalPoint.fromWorld(client, nw);
		LocalPoint lne = LocalPoint.fromWorld(client, ne);
		LocalPoint lse = LocalPoint.fromWorld(client, se);

		if (lsw == null || lnw == null || lne == null || lse == null)
		{
			return;
		}

		Polygon poly = new Polygon();

		addPoint(poly, lsw);
		addPoint(poly, lnw);
		addPoint(poly, lne);
		addPoint(poly, lse);

		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(2));
		graphics.drawPolygon(poly);
	}

	private void addPoint(Polygon poly, LocalPoint lp)
	{
		Point p = Perspective.localToCanvas(client, lp, client.getWorldView(lp.getWorldView()).getPlane());
		if (p != null)
		{
			poly.addPoint(p.getX(), p.getY());
		}
	}

	private void renderTile(Graphics2D graphics, WorldPoint worldPoint, Color color)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);
		if (localPoint == null)
		{
			return;
		}

		Polygon poly = Perspective.getCanvasTilePoly(client, localPoint);
		if (poly == null)
		{
			return;
		}

		graphics.setColor(color);
		graphics.fillPolygon(poly);

		graphics.setColor(color);
		graphics.drawPolygon(poly);
	}
}
