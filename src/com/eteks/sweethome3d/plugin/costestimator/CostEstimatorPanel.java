/*
 * CostEstimatorPanel.java - Stage BOM panel
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Panel for one construction stage: DIY/Hire radio buttons, a BOM table
 * (labor rows appear inline, styled in gray), and stage subtotals.
 *
 * Columns: Material | Qty | Unit | Unit Cost | Total
 */
public class CostEstimatorPanel extends JPanel {
  private static final long serialVersionUID = 1L;
  private static final DecimalFormat CURRENCY = new DecimalFormat("$#,##0.00");
  private static final DecimalFormat QTY_FMT  = new DecimalFormat("#,##0.##");
  private static final Color LABOR_FG = new Color(100, 100, 100);

  private List<MaterialLineItem> items;
  private final BOMTableModel tableModel;
  private final JLabel matTotalLabel;
  private final JLabel laborTotalLabel;
  private final JLabel stageTotalLabel;
  private final JRadioButton diyButton;
  private final JRadioButton hireButton;
  private final List<ActionListener> diyListeners = new ArrayList<>();

  public CostEstimatorPanel(List<MaterialLineItem> items, boolean isDIY) {
    super(new BorderLayout(5, 5));
    this.items = new ArrayList<>(items);

    // ── DIY / Hire row ──────────────────────────────────────────────────────
    diyButton  = new JRadioButton("DIY",      isDIY);
    hireButton = new JRadioButton("Hire Out", !isDIY);
    ButtonGroup group = new ButtonGroup();
    group.add(diyButton);
    group.add(hireButton);

    ActionListener toggle = e -> fireDIYChanged();
    diyButton.addActionListener(toggle);
    hireButton.addActionListener(toggle);

    JPanel diyRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
    diyRow.add(diyButton);
    diyRow.add(hireButton);
    add(diyRow, BorderLayout.NORTH);

    // ── Table ───────────────────────────────────────────────────────────────
    tableModel = new BOMTableModel(this.items);
    JTable table = new JTable(tableModel);
    table.setRowHeight(20);
    table.setShowGrid(true);
    table.setGridColor(Color.LIGHT_GRAY);
    table.setDefaultRenderer(Object.class, new LaborAwareRenderer());

    table.getColumnModel().getColumn(0).setPreferredWidth(220); // Material
    table.getColumnModel().getColumn(1).setPreferredWidth(65);  // Qty
    table.getColumnModel().getColumn(2).setPreferredWidth(60);  // Unit
    table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Unit Cost
    table.getColumnModel().getColumn(4).setPreferredWidth(90);  // Total

    JScrollPane scroll = new JScrollPane(table);
    scroll.setPreferredSize(new Dimension(540, 260));
    add(scroll, BorderLayout.CENTER);

    // ── Totals row ──────────────────────────────────────────────────────────
    matTotalLabel   = new JLabel();
    laborTotalLabel = new JLabel();
    stageTotalLabel = new JLabel();
    stageTotalLabel.setFont(stageTotalLabel.getFont().deriveFont(Font.BOLD, 13f));

    JPanel totalsRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
    totalsRow.add(new JLabel("Materials:"));
    totalsRow.add(matTotalLabel);
    totalsRow.add(new JLabel("  Labor:"));
    totalsRow.add(laborTotalLabel);
    totalsRow.add(new JLabel("  Stage Total:"));
    totalsRow.add(stageTotalLabel);
    add(totalsRow, BorderLayout.SOUTH);

    updateTotals();
  }

  /** Replace the item list and refresh display. */
  public void setItems(List<MaterialLineItem> newItems) {
    this.items = new ArrayList<>(newItems);
    tableModel.setItems(this.items);
    updateTotals();
  }

  public boolean isDIY() { return diyButton.isSelected(); }

  public void addDIYChangeListener(ActionListener l) { diyListeners.add(l); }

  private void fireDIYChanged() {
    ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "diyChanged");
    for (ActionListener l : diyListeners) l.actionPerformed(e);
  }

  private void updateTotals() {
    float mat = 0f, labor = 0f;
    for (MaterialLineItem item : items) {
      mat   += item.getMaterialTotal();
      labor += item.getLaborTotal();
    }
    matTotalLabel.setText(CURRENCY.format(mat));
    laborTotalLabel.setText(CURRENCY.format(labor));
    stageTotalLabel.setText(CURRENCY.format(mat + labor));
  }

  // ---------------------------------------------------------------------------
  // Table model — 5 columns: Material | Qty | Unit | Unit Cost | Total
  // ---------------------------------------------------------------------------

  private static class BOMTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final String[] COLUMNS = { "Material", "Qty", "Unit", "Unit Cost", "Total" };

    private List<MaterialLineItem> items;

    BOMTableModel(List<MaterialLineItem> items) { this.items = items; }

    void setItems(List<MaterialLineItem> items) {
      this.items = items;
      fireTableDataChanged();
    }

    List<MaterialLineItem> getItems() { return items; }

    @Override public int getRowCount()    { return items.size(); }
    @Override public int getColumnCount() { return COLUMNS.length; }
    @Override public String getColumnName(int col) { return COLUMNS[col]; }
    @Override public Class<?> getColumnClass(int col) { return String.class; }
    @Override public boolean isCellEditable(int row, int col) { return false; }

    @Override
    public Object getValueAt(int row, int col) {
      MaterialLineItem item = items.get(row);
      switch (col) {
        case 0: return item.getName();
        case 1: return QTY_FMT.format(item.getQuantity());
        case 2: return item.getUnit();
        case 3: return CURRENCY.format(item.getUnitCost());
        case 4: return CURRENCY.format(item.getTotal());
        default: return "";
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Renderer — labor rows shown in muted gray, right-aligned for numerics
  // ---------------------------------------------------------------------------

  private class LaborAwareRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

      super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

      boolean isLabor = row < tableModel.getItems().size()
          && tableModel.getItems().get(row).isLabor();

      setForeground(isSelected ? table.getSelectionForeground()
                               : (isLabor ? LABOR_FG : table.getForeground()));
      setFont(isLabor ? getFont().deriveFont(Font.ITALIC) : getFont().deriveFont(Font.PLAIN));

      // Right-align Qty, Unit Cost, Total columns
      setHorizontalAlignment(col >= 1 ? JLabel.RIGHT : JLabel.LEFT);

      return this;
    }
  }
}
