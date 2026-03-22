/*
 * CostEstimatorDialog.java - Cost estimator main dialog
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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.UserPreferences;

/**
 * Dialog for viewing and configuring cost estimates.
 */
public class CostEstimatorDialog extends JDialog {
  private static final long serialVersionUID = 1L;
  private static final String RATES_PREFERENCE_KEY = "costEstimator.rates";
  private static final String WET_ROOMS_PREFERENCE_KEY = "costEstimator.wetRooms";

  private Home home;
  private UserPreferences preferences;
  private CostRates rates;
  private List<Integer> wetRoomIndices;
  private CostEstimatorPanel estimatorPanel;

  public CostEstimatorDialog(Frame owner, Home home, UserPreferences preferences) {
    super(owner, "Cost Estimator", true);
    this.home = home;
    this.preferences = preferences;

    loadRates();
    loadWetRoomIndices();

    CostReport report = CostCalculator.calculate(home, rates, wetRoomIndices);
    estimatorPanel = new CostEstimatorPanel(report);

    // Note: To listen for home changes, you would need to add listeners to walls,
    // rooms, and furniture collections. For now, estimates are static until dialog is reopened.

    // Main layout
    setLayout(new BorderLayout(5, 5));
    add(estimatorPanel, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(Box.createHorizontalGlue());

    JButton editRatesButton = new JButton("Edit Rates...");
    editRatesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showEditRatesDialog();
      }
    });
    buttonPanel.add(editRatesButton);

    JButton exportButton = new JButton("Export to PDF");
    exportButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        exportToPDF();
      }
    });
    buttonPanel.add(exportButton);

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    buttonPanel.add(closeButton);

    add(buttonPanel, BorderLayout.SOUTH);

    setSize(600, 450);
    setLocationRelativeTo(owner);
  }

  private void loadRates() {
    this.rates = new CostRates();
    // TODO: Load from preferences if saved
  }

  private void loadWetRoomIndices() {
    this.wetRoomIndices = new ArrayList<>();
    // TODO: Load from preferences if saved
  }

  private void saveRates() {
    // TODO: Save to preferences
  }

  private void saveWetRoomIndices() {
    // TODO: Save to preferences
  }

  private void refreshEstimate() {
    CostReport report = CostCalculator.calculate(home, rates, wetRoomIndices);
    estimatorPanel.updateReport(report);
  }

  private void showEditRatesDialog() {
    CostRatesDialog dialog = new CostRatesDialog(this, rates, home.getRooms());
    dialog.setVisible(true);

    if (dialog.wasOkClicked()) {
      CostRates newRates = dialog.getRates();
      List<Integer> newWetRooms = dialog.getWetRoomIndices();

      this.rates = newRates;
      this.wetRoomIndices = newWetRooms;
      saveRates();
      saveWetRoomIndices();
      refreshEstimate();
    }
  }

  private void exportToPDF() {
    // TODO: Implement PDF export
    // Could use iText (already available) or Apache PDFBox
    javax.swing.JOptionPane.showMessageDialog(this,
        "PDF export not yet implemented. Coming soon!",
        "PDF Export", javax.swing.JOptionPane.INFORMATION_MESSAGE);
  }
}
