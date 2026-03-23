/*
 * CostEstimatorDialog.java - BOM + Cost estimator main dialog
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Main BOM + cost estimator dialog.
 * Shows one tab per construction stage with material quantities and costs,
 * plus a Summary tab with stage-level totals.
 */
public class CostEstimatorDialog extends JDialog {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(CostEstimatorDialog.class.getName());
  private static final DecimalFormat CURRENCY = new DecimalFormat("$#,##0.00");

  private final Home home;
  private final UserPreferences preferences;
  private BOMSettings settings;
  private BOMReport report;

  // One panel per stage, keyed by stage
  private final Map<BOMReport.Stage, CostEstimatorPanel> stagePanels = new EnumMap<>(BOMReport.Stage.class);
  private SummaryTableModel summaryModel;

  public CostEstimatorDialog(java.awt.Frame owner, Home home, UserPreferences preferences) {
    super(owner, "BOM & Cost Estimator", true);
    this.home        = home;
    this.preferences = preferences;

    LOG.info("[CostEstimatorDialog] Initializing dialog");

    loadSettings();
    report = BOMCalculator.calculate(home, settings);

    LOG.info("[CostEstimatorDialog] Initial report grand total: "
        + CURRENCY.format(report.getGrandTotal()));

    buildUI();
    setSize(680, 520);
    setLocationRelativeTo(owner);
  }

  // ---------------------------------------------------------------------------
  // UI construction
  // ---------------------------------------------------------------------------

  private void buildUI() {
    setLayout(new BorderLayout(5, 5));

    JTabbedPane tabs = new JTabbedPane();

    // One tab per stage
    for (BOMReport.Stage stage : BOMReport.Stage.values()) {
      List<MaterialLineItem> items = report.getItems(stage);
      boolean isDIY = stageIsDIY(stage);
      CostEstimatorPanel panel = new CostEstimatorPanel(items, isDIY);

      panel.addDIYChangeListener(e -> {
        boolean diy = panel.isDIY();
        LOG.info("[CostEstimatorDialog] DIY toggled for " + stage.getDisplayName() + " → " + diy);
        setStageDIY(stage, diy);
        refreshAll();
      });

      stagePanels.put(stage, panel);
      tabs.addTab(stage.getDisplayName(), panel);
    }

    // Summary tab
    tabs.addTab("Summary", buildSummaryTab());

    add(tabs, BorderLayout.CENTER);

    // Button row
    JPanel buttons = new JPanel();
    buttons.add(Box.createHorizontalGlue());

    JButton settingsBtn = new JButton("Settings...");
    settingsBtn.addActionListener(e -> showSettingsDialog());
    buttons.add(settingsBtn);

    JButton closeBtn = new JButton("Close");
    closeBtn.addActionListener(e -> dispose());
    buttons.add(closeBtn);

    add(buttons, BorderLayout.SOUTH);
  }

  private JPanel buildSummaryTab() {
    summaryModel = new SummaryTableModel(report);
    JTable table = new JTable(summaryModel);
    table.setRowHeight(22);
    table.setShowGrid(true);
    table.setGridColor(Color.LIGHT_GRAY);
    table.getColumnModel().getColumn(0).setPreferredWidth(120);
    table.getColumnModel().getColumn(1).setPreferredWidth(110);
    table.getColumnModel().getColumn(2).setPreferredWidth(110);
    table.getColumnModel().getColumn(3).setPreferredWidth(110);

    DefaultTableCellRenderer right = new DefaultTableCellRenderer();
    right.setHorizontalAlignment(JLabel.RIGHT);
    for (int c = 1; c <= 3; c++) {
      table.getColumnModel().getColumn(c).setCellRenderer(right);
    }

    JScrollPane scroll = new JScrollPane(table);
    scroll.setPreferredSize(new Dimension(460, 220));

    JLabel grandLabel = new JLabel();
    grandLabel.setFont(grandLabel.getFont().deriveFont(Font.BOLD, 14f));
    updateGrandLabel(grandLabel);

    // Store reference so refreshAll can update it
    this.grandTotalLabel = grandLabel;

    JPanel totalsRow = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 12, 4));
    totalsRow.add(new JLabel("Grand Total:"));
    totalsRow.add(grandLabel);

    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.add(scroll,     BorderLayout.CENTER);
    panel.add(totalsRow,  BorderLayout.SOUTH);
    return panel;
  }

  private JLabel grandTotalLabel;

  private void updateGrandLabel(JLabel label) {
    label.setText(CURRENCY.format(report.getGrandTotal()));
  }

  // ---------------------------------------------------------------------------
  // Refresh
  // ---------------------------------------------------------------------------

  /** Recalculate the BOM from current settings and update all panels. */
  private void refreshAll() {
    LOG.info("[CostEstimatorDialog] Refreshing BOM report");
    report = BOMCalculator.calculate(home, settings);

    for (BOMReport.Stage stage : BOMReport.Stage.values()) {
      CostEstimatorPanel panel = stagePanels.get(stage);
      if (panel != null) {
        panel.setItems(report.getItems(stage));
      }
    }

    if (summaryModel != null) {
      summaryModel.setReport(report);
    }
    if (grandTotalLabel != null) {
      updateGrandLabel(grandTotalLabel);
    }

    LOG.info("[CostEstimatorDialog] Refresh complete, grand total: "
        + CURRENCY.format(report.getGrandTotal()));
  }

  // ---------------------------------------------------------------------------
  // Settings dialog
  // ---------------------------------------------------------------------------

  private void showSettingsDialog() {
    LOG.info("[CostEstimatorDialog] Opening settings dialog");
    BOMSettingsDialog dialog = new BOMSettingsDialog(this, settings, home.getRooms());
    dialog.setVisible(true);

    if (dialog.wasOkClicked()) {
      LOG.info("[CostEstimatorDialog] Settings updated, refreshing");
      // Sync DIY toggle UI to match updated settings
      for (BOMReport.Stage stage : BOMReport.Stage.values()) {
        // (Panel DIY radio reflects settings; we just need to refresh data)
      }
      refreshAll();
    }
  }

  // ---------------------------------------------------------------------------
  // Settings persistence (stubs — TODO: use UserPreferences)
  // ---------------------------------------------------------------------------

  private void loadSettings() {
    this.settings = new BOMSettings();
    LOG.info("[CostEstimatorDialog] Loaded default BOM settings");
  }

  // ---------------------------------------------------------------------------
  // DIY flag helpers
  // ---------------------------------------------------------------------------

  private boolean stageIsDIY(BOMReport.Stage stage) {
    switch (stage) {
      case FRAMING:    return settings.getFraming().isDIY;
      case DRYWALL:    return settings.getDrywall().isDIY;
      case PAINT:      return settings.getPaint().isDIY;
      case ELECTRICAL: return settings.getElectrical().isDIY;
      case PLUMBING:   return settings.getPlumbing().isDIY;
      case FLOORING:   return settings.getFlooring().isDIY;
      default:         return false;
    }
  }

  private void setStageDIY(BOMReport.Stage stage, boolean diy) {
    switch (stage) {
      case FRAMING:    settings.getFraming().isDIY    = diy; break;
      case DRYWALL:    settings.getDrywall().isDIY    = diy; break;
      case PAINT:      settings.getPaint().isDIY      = diy; break;
      case ELECTRICAL: settings.getElectrical().isDIY = diy; break;
      case PLUMBING:   settings.getPlumbing().isDIY   = diy; break;
      case FLOORING:   settings.getFlooring().isDIY   = diy; break;
    }
  }

  // ---------------------------------------------------------------------------
  // Summary table model
  // ---------------------------------------------------------------------------

  private static class SummaryTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = { "Stage", "Materials", "Labor", "Total" };

    private BOMReport report;

    SummaryTableModel(BOMReport report) {
      this.report = report;
    }

    void setReport(BOMReport report) {
      this.report = report;
      fireTableDataChanged();
    }

    @Override public int getRowCount()    { return BOMReport.Stage.values().length; }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }
    @Override public Class<?> getColumnClass(int col) { return String.class; }
    @Override public boolean isCellEditable(int row, int col) { return false; }

    @Override
    public Object getValueAt(int row, int col) {
      BOMReport.Stage stage = BOMReport.Stage.values()[row];
      switch (col) {
        case 0: return stage.getDisplayName();
        case 1: return CURRENCY.format(report.getStageMaterialTotal(stage));
        case 2: return CURRENCY.format(report.getStageLaborTotal(stage));
        case 3: return CURRENCY.format(report.getStageTotal(stage));
        default: return "";
      }
    }
  }
}
