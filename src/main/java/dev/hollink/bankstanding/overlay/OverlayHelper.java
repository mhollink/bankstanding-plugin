package dev.hollink.bankstanding.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Arrays;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public interface OverlayHelper
{

	default void setPanelWidth(int width, PanelComponent panel)
	{
		panel.setPreferredSize(new Dimension(width, 0));
	}

	default void addPanelPadding(PanelComponent panel)
	{
		panel.setBorder(new Rectangle(6, 4, 6, 4));
	}

	default void addTitle(String title, PanelComponent panel) {
		panel.getChildren().add(
			TitleComponent.builder()
				.text(title)
				.color(Color.WHITE)
				.build()
		);
	}

	default void addLabelledText(String left, String right, PanelComponent panel)
	{
		panel.getChildren().add(LineComponent.builder()
			.left(left).leftColor(Color.YELLOW)
			.right(right).rightColor(Color.WHITE)
			.build());
	}

	default void addText(String left, String right, PanelComponent panel)
	{
		panel.getChildren().add(LineComponent.builder()
			.left(left).leftColor(Color.WHITE)
			.right(right).rightColor(Color.WHITE)
			.build());
	}

	default void addRightOnlyText(String right, PanelComponent panel)
	{
		panel.getChildren().add(LineComponent.builder()
			.right(right).rightColor(Color.WHITE)
			.build());
	}

	default void addLineBreak(PanelComponent panel) {
		panel.getChildren().add(LineComponent.builder().build());
	}

	default void addProgressBar(float progress, PanelComponent panel)
	{
		panel.getChildren().add(buildProgressBarComponent(progress));
	}

	private ProgressBarComponent buildProgressBarComponent(float percentage)
	{
		ProgressBarComponent progressBar = new ProgressBarComponent();
		progressBar.setMinimum(0);
		progressBar.setMaximum(1);
		progressBar.setValue(percentage);
		progressBar.setForegroundColor(Color.GREEN);
		return progressBar;
	}
}
