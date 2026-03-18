package dev.hollink.bankstanding.panel;

import dev.hollink.bankstanding.config.BankLocation;
import dev.hollink.bankstanding.domain.BankStats;
import dev.hollink.bankstanding.state.bankstats.BankStatsManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class BankstandingPanel extends PluginPanel
{
	private static final Color COLOR_IDLE = new Color(210, 75, 75);
	private static final Color COLOR_ACTIVE = new Color(75, 190, 110);
	private static final Color COLOR_GOLD = new Color(200, 170, 100);
	private static final Color BG_ROW_ODD = new Color(37, 37, 37);
	private static final Color BG_ROW_EVEN = new Color(45, 45, 45);

	private final BankStatsManager bankStatsManager;

	private final JLabel totalLabel = new JLabel();
	private final JLabel idleLabel = new JLabel();
	private final JLabel activeLabel = new JLabel();
	private final JPanel listPanel = new JPanel();

	private boolean showSession = true;

	@Inject
	public BankstandingPanel(BankStatsManager bankStatsManager)
	{
		super(false);
		this.bankStatsManager = bankStatsManager;
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);
		setBorder(new EmptyBorder(6, 6, 6, 6));

		add(buildHeader(), BorderLayout.NORTH);

		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
		JScrollPane scroll = new JScrollPane(listPanel);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scroll.getViewport().setBackground(ColorScheme.DARK_GRAY_COLOR);
		add(scroll, BorderLayout.CENTER);
	}

	private JPanel buildHeader()
	{
		JPanel header = new JPanel();
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(new EmptyBorder(0, 0, 5, 0));

		// Row 1: title
		JLabel title = new JLabel("Bankstanding Tracker");
		title.setForeground(COLOR_GOLD);
		title.setFont(FontManager.getRunescapeBoldFont());
		title.setAlignmentX(Component.LEFT_ALIGNMENT);
		header.add(title);
		header.add(Box.createVerticalStrut(4));

		// Row 2: toggles + reset
		JPanel controlRow = new JPanel(new BorderLayout());
		controlRow.setBackground(ColorScheme.DARK_GRAY_COLOR);
		controlRow.setAlignmentX(Component.LEFT_ALIGNMENT);

		JPanel togglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		togglePanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		JButton sessionBtn = new JButton("Session");
		JButton allTimeBtn = new JButton("All Time");
		styleToggle(sessionBtn, true);
		styleToggle(allTimeBtn, false);

		sessionBtn.addActionListener(e -> {
			showSession = true;
			styleToggle(sessionBtn, true);
			styleToggle(allTimeBtn, false);
			refresh();
		});
		allTimeBtn.addActionListener(e -> {
			showSession = false;
			styleToggle(sessionBtn, false);
			styleToggle(allTimeBtn, true);
			refresh();
		});

		togglePanel.add(sessionBtn);
		togglePanel.add(allTimeBtn);
		controlRow.add(togglePanel, BorderLayout.WEST);

		JButton resetBtn = new JButton("Reset");
		styleResetButton(resetBtn);
//		resetBtn.addActionListener(e -> confirmReset());
		controlRow.add(resetBtn, BorderLayout.EAST);

		header.add(controlRow);
		header.add(Box.createVerticalStrut(4));

		// Row 3: total / idle / active
		totalLabel.setForeground(Color.LIGHT_GRAY);
		totalLabel.setFont(FontManager.getRunescapeSmallFont());
		totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		idleLabel.setForeground(COLOR_IDLE);
		idleLabel.setFont(FontManager.getRunescapeSmallFont());
		idleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		activeLabel.setForeground(COLOR_ACTIVE);
		activeLabel.setFont(FontManager.getRunescapeSmallFont());
		activeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		header.add(totalLabel);
		header.add(Box.createVerticalStrut(1));
		header.add(idleLabel);
		header.add(Box.createVerticalStrut(1));
		header.add(activeLabel);
		header.add(Box.createVerticalStrut(5));

		// Separator
		JSeparator sep = new JSeparator();
		sep.setForeground(new Color(55, 55, 55));
		sep.setAlignmentX(Component.LEFT_ALIGNMENT);
		sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		header.add(sep);

		return header;
	}

	private void styleToggle(JButton btn, boolean active)
	{
		btn.setFont(FontManager.getRunescapeSmallFont());
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(active ? COLOR_GOLD : new Color(60, 60, 60)),
			new EmptyBorder(1, 5, 1, 5)));
		btn.setBackground(active ? new Color(60, 52, 32) : new Color(48, 48, 48));
		btn.setForeground(active ? COLOR_GOLD : new Color(150, 150, 150));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	private void styleResetButton(JButton btn)
	{
		btn.setFont(FontManager.getRunescapeSmallFont());
		btn.setFocusPainted(false);
		btn.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(90, 45, 45)),
			new EmptyBorder(1, 5, 1, 5)));
		btn.setBackground(new Color(55, 30, 30));
		btn.setForeground(new Color(200, 100, 100));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	public void refresh()
	{
		Map<BankLocation, BankStats> stats = showSession
			? bankStatsManager.getSessionStats()
			: bankStatsManager.getAllTimeStats();

		BankLocation live = bankStatsManager.getBankLocation().orElse(null);

		long totTicks = stats.values().stream().mapToLong(BankStats::getTotalTicks).sum();
		long idleTicks = stats.values().stream().mapToLong(BankStats::getIdleTicks).sum();
		long actTicks = stats.values().stream().mapToLong(BankStats::getActiveTicks).sum();
		int idlePct = totTicks > 0 ? (int) (idleTicks * 100 / totTicks) : 0;
		int actPct = totTicks > 0 ? (int) (actTicks * 100 / totTicks) : 0;

		totalLabel.setText("Total   " + BankStats.formatTicksToTime(totTicks));
		idleLabel.setText("Idle    " + BankStats.formatTicksToTime(idleTicks) + "  (" + idlePct + "%)");
		activeLabel.setText("Active  " + BankStats.formatTicksToTime(actTicks) + "  (" + actPct + "%)");

		listPanel.removeAll();

		List<Map.Entry<BankLocation, BankStats>> sorted = stats.entrySet().stream()
			.sorted(Comparator.comparingLong((Map.Entry<BankLocation, BankStats> e) -> e.getValue().getTotalTicks()).reversed())
			.collect(Collectors.toList());

		if (sorted.isEmpty())
		{
			JLabel empty = new JLabel("No bank visits tracked yet.");
			empty.setForeground(new Color(100, 100, 100));
			empty.setFont(FontManager.getRunescapeSmallFont());
			empty.setBorder(new EmptyBorder(8, 2, 0, 0));
			listPanel.add(empty);
		}

		boolean odd = true;
		for (Map.Entry<BankLocation, BankStats> entry : sorted)
		{
			listPanel.add(buildBankRow(entry.getKey(), entry.getValue(), odd));
			odd = !odd;
		}

		listPanel.revalidate();
		listPanel.repaint();
	}


	private JPanel buildBankRow(BankLocation bank, BankStats stats, boolean odd)
	{
		JPanel row = new JPanel(new BorderLayout());
		row.setBackground(odd ? BG_ROW_ODD : BG_ROW_EVEN);
		row.setBorder(new EmptyBorder(3, 6, 3, 4));

		// Left: name + idle/active %
		JPanel left = new JPanel();
		left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
		left.setOpaque(false);

		JLabel nameLabel = new JLabel(bank.name());
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setFont(FontManager.getRunescapeSmallFont());
		left.add(nameLabel);

		JLabel splitLabel = new JLabel(String.format(
			"<html><font color='#d24b4b'>%d%%</font>"
				+ "<font color='#505050'> / </font>"
				+ "<font color='#4bbe6e'>%d%%</font></html>",
			stats.idlePercent(), stats.activePercent()));
		splitLabel.setFont(FontManager.getRunescapeSmallFont());
		left.add(splitLabel);

		// Right: time + visits
		JPanel right = new JPanel();
		right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
		right.setOpaque(false);

		JLabel timeLabel = new JLabel(BankStats.formatTicksToTime(stats.getTotalTicks()));
		timeLabel.setForeground(new Color(160, 160, 160));
		timeLabel.setFont(FontManager.getRunescapeSmallFont());
		timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		right.add(timeLabel);

		JLabel visitsLabel = new JLabel(stats.getVisits() + (stats.getVisits() == 1 ? " visit" : " visits"));
		visitsLabel.setForeground(new Color(80, 80, 80));
		visitsLabel.setFont(FontManager.getRunescapeSmallFont());
		visitsLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		right.add(visitsLabel);

		row.add(left, BorderLayout.WEST);
		row.add(right, BorderLayout.EAST);

		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));

		return row;
	}
}
