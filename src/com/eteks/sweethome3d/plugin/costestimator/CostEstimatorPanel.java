/*
 * CostEstimatorPanel.java - Cost estimator UI panel
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
import java.awt.Dimension;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Panel displaying cost estimate breakdown in a table format.
 */
public class CostEstimatorPanel extends JPanel {
  private static final long serialVersionUID = 1L;
  private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("$#,##0.00");
  private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0.00");

  private JTable table;
  private JLabel totalLabel;
  private CostReport report;

  public CostEstimatorPanel(CostReport report) {
    super(new BorderLayout(5, 5));
    this.report = report;

    // Table
    table = new JTable(new CostTableModel(report));
    table.setRowHeight(20);
    table.getColumnModel().getColumn(0).setPreferredWidth(180);
    table.getColumnModel().getColumn(1).setPreferredWidth(100);
    table.getColumnModel().getColumn(2).setPreferredWidth(80);
    table.getColumnModel().getColumn(3).setPreferredWidth(100);

    // Right-align numeric columns
    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
    table.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
    table.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
    table.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setPreferredSize(new Dimension(500, 300));
    add(scrollPane, BorderLayout.CENTER);

    // Total panel
    JPanel totalPanel = new JPanel(new BorderLayout());
    totalLabel = new JLabel();
    updateTotalLabel();
    Font boldFont = totalLabel.getFont().deriveFont(Font.BOLD, 14f);
    totalLabel.setFont(boldFont);
    totalPanel.add(totalLabel, BorderLayout.EAST);
    add(totalPanel, BorderLayout.SOUTH);
  }

  public void updateReport(CostReport newReport) {
    this.report = newReport;
    ((CostTableModel) table.getModel()).setReport(newReport);
    updateTotalLabel();
  }

  private void updateTotalLabel() {
    totalLabel.setText("Total Estimate: " + CURRENCY_FORMAT.format(report.getGrandTotal()));
  }

  public CostReport getReport() {
    return report;
  }

  // Table model for displaying cost items
  private static class CostTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private CostReport report;

    public CostTableModel(CostReport report) {
      this.report = report;
    }

    public void setReport(CostReport report) {
      this.report = report;
      fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
      return report.getItems().length;
    }

    @Override
    public int getColumnCount() {
      return 4;
    }

    @Override
    public String getColumnName(int col) {
      switch (col) {
        case 0:
          return "Category";
        case 1:
          return "Quantity";
        case 2:
          return "Rate";
        case 3:
          return "Total";
        default:
          return "";
      }
    }

    @Override
    public Object getValueAt(int row, int col) {
      CostReport.LineItem item = report.getItems()[row];
      switch (col) {
        case 0:
          return item.category;
        case 1:
          return item.quantity;
        case 2:
          return item.rate > 0 ? CURRENCY_FORMAT.format(item.rate) : "";
        case 3:
          return CURRENCY_FORMAT.format(item.total);
        default:
          return "";
      }
    }

    @Override
    public Class<?> getColumnClass(int col) {
      return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
      return false;
    }
  }
}
