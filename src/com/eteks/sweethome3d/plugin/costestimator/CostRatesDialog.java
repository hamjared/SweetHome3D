/*
 * CostRatesDialog.java - Dialog for editing cost rates
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.eteks.sweethome3d.model.Room;

/**
 * Dialog for editing cost rates and flagging wet rooms.
 */
public class CostRatesDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  private CostRates rates;
  private CostRates originalRates;
  private List<Integer> wetRoomIndices;
  private List<JCheckBox> wetRoomCheckboxes;
  private boolean okClicked;

  public CostRatesDialog(JDialog owner, CostRates rates, List<Room> rooms) {
    super(owner, "Edit Rates", true);
    this.originalRates = rates;
    this.rates = new CostRates(rates);
    this.wetRoomIndices = new ArrayList<>();
    this.wetRoomCheckboxes = new ArrayList<>();

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());

    // Tabbed pane
    JTabbedPane tabbedPane = new JTabbedPane();

    // Rates tab
    tabbedPane.addTab("Rates", createRatesPanel());

    // Wet rooms tab
    tabbedPane.addTab("Wet Rooms", createWetRoomsPanel(rooms));

    add(tabbedPane, BorderLayout.CENTER);

    // Button panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(Box.createHorizontalGlue());

    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        okClicked = true;
        updateWetRoomIndices();
        dispose();
      }
    });
    buttonPanel.add(okButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        okClicked = false;
        dispose();
      }
    });
    buttonPanel.add(cancelButton);

    add(buttonPanel, BorderLayout.SOUTH);

    setSize(400, 500);
    setLocationRelativeTo(owner);
  }

  private JPanel createRatesPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    NumberFormat format = new DecimalFormat("0.00");

    int row = 0;

    // Framing
    addRateField(panel, gbc, row++, "Framing (per linear ft):",
        new JFormattedTextField(format), rates.getFramingPerLinearFoot(),
        (value) -> rates.setFramingPerLinearFoot(value));

    // Drywall
    addRateField(panel, gbc, row++, "Drywall (per sq ft):",
        new JFormattedTextField(format), rates.getDrywallPerSqFt(),
        (value) -> rates.setDrywallPerSqFt(value));

    // Paint
    addRateField(panel, gbc, row++, "Paint (per sq ft):",
        new JFormattedTextField(format), rates.getPaintPerSqFt(),
        (value) -> rates.setPaintPerSqFt(value));

    // Flooring
    addRateField(panel, gbc, row++, "Flooring (per sq ft):",
        new JFormattedTextField(format), rates.getFlooringPerSqFt(),
        (value) -> rates.setFlooringPerSqFt(value));

    // Electrical base
    addRateField(panel, gbc, row++, "Electrical base (per room):",
        new JFormattedTextField(format), rates.getElectricalBasePerRoom(),
        (value) -> rates.setElectricalBasePerRoom(value));

    // Electrical fixtures
    addRateField(panel, gbc, row++, "Electrical (per fixture):",
        new JFormattedTextField(format), rates.getElectricalPerFixture(),
        (value) -> rates.setElectricalPerFixture(value));

    // Plumbing standard
    addRateField(panel, gbc, row++, "Plumbing (per standard room):",
        new JFormattedTextField(format), rates.getPlumbingPerRoom(),
        (value) -> rates.setPlumbingPerRoom(value));

    // Plumbing wet
    addRateField(panel, gbc, row++, "Plumbing (per wet room):",
        new JFormattedTextField(format), rates.getPlumbingPerWetRoom(),
        (value) -> rates.setPlumbingPerWetRoom(value));

    // Door
    addRateField(panel, gbc, row++, "Door:",
        new JFormattedTextField(format), rates.getPerDoor(),
        (value) -> rates.setPerDoor(value));

    // Window
    addRateField(panel, gbc, row++, "Window:",
        new JFormattedTextField(format), rates.getPerWindow(),
        (value) -> rates.setPerWindow(value));

    JScrollPane scrollPane = new JScrollPane(panel);
    JPanel wrapperPanel = new JPanel(new BorderLayout());
    wrapperPanel.add(scrollPane, BorderLayout.CENTER);
    return wrapperPanel;
  }

  private void addRateField(JPanel panel, GridBagConstraints gbc, int row, String label,
      JFormattedTextField field, float value,
      ValueSetter setter) {
    field.setValue(value);
    field.setColumns(10);

    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 1.0;
    panel.add(new JLabel(label), gbc);

    gbc.gridx = 1;
    field.addPropertyChangeListener("value", evt -> {
      Number num = (Number) field.getValue();
      if (num != null) {
        setter.setValue(num.floatValue());
      }
    });
    panel.add(field, gbc);
  }

  private JPanel createWetRoomsPanel(List<Room> rooms) {
    JPanel checkboxPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.WEST;

    int row = 0;
    for (int i = 0; i < rooms.size(); i++) {
      Room room = rooms.get(i);
      String roomName = room.getName() != null ? room.getName() : "Room " + (i + 1);

      JCheckBox checkbox = new JCheckBox(roomName);
      wetRoomCheckboxes.add(checkbox);

      gbc.gridx = 0;
      gbc.gridy = row++;
      checkboxPanel.add(checkbox, gbc);
    }

    // Return a scrollable panel
    JScrollPane scrollPane = new JScrollPane(checkboxPanel);
    scrollPane.setPreferredSize(new Dimension(300, 300));

    // Wrap scrollpane in a panel since we need to return JPanel
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
  }

  private void updateWetRoomIndices() {
    wetRoomIndices.clear();
    for (int i = 0; i < wetRoomCheckboxes.size(); i++) {
      if (wetRoomCheckboxes.get(i).isSelected()) {
        wetRoomIndices.add(i);
      }
    }
  }

  public boolean wasOkClicked() {
    return okClicked;
  }

  public CostRates getRates() {
    return rates;
  }

  public List<Integer> getWetRoomIndices() {
    return wetRoomIndices;
  }

  @FunctionalInterface
  private interface ValueSetter {
    void setValue(float value);
  }
}
