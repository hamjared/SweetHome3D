/*
 * BOMSettingsDialog.java - Dialog for editing BOM settings and rates
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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.WindowConstants;

import com.eteks.sweethome3d.model.Room;

/**
 * Dialog for editing all BOM settings: global lumber/spacing options,
 * per-stage material and labor rates, DIY toggles, and wet room flags.
 */
public class BOMSettingsDialog extends JDialog {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(BOMSettingsDialog.class.getName());

  private final BOMSettings settings;
  private boolean okClicked = false;

  // Wet rooms
  private final List<JCheckBox> wetRoomBoxes = new ArrayList<>();
  private final List<Room> rooms;

  // Global controls
  private JRadioButton radio2x4;
  private JRadioButton radio2x6;
  private JRadioButton spacing16;
  private JRadioButton spacing24;
  private JCheckBox    allFloatingBox;

  public BOMSettingsDialog(JDialog owner, BOMSettings settings, List<Room> rooms) {
    super(owner, "BOM Settings", true);
    this.settings = settings;
    this.rooms = rooms;

    LOG.info("[BOMSettingsDialog] Opening settings dialog");

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setLayout(new BorderLayout());

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Global",     createGlobalTab());
    tabs.addTab("Framing",    createFramingTab());
    tabs.addTab("Drywall",    createDrywallTab());
    tabs.addTab("Paint",      createPaintTab());
    tabs.addTab("Flooring",   createFlooringTab());
    tabs.addTab("Electrical", createElectricalTab());
    tabs.addTab("Plumbing",   createPlumbingTab());
    tabs.addTab("Wet Rooms",  createWetRoomsTab());

    add(tabs, BorderLayout.CENTER);

    // Buttons
    JPanel buttons = new JPanel();
    buttons.add(Box.createHorizontalGlue());

    JButton ok = new JButton("OK");
    ok.addActionListener(e -> {
      applyAll();
      okClicked = true;
      LOG.info("[BOMSettingsDialog] OK clicked — settings applied");
      dispose();
    });
    buttons.add(ok);

    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(e -> {
      LOG.info("[BOMSettingsDialog] Cancel clicked");
      dispose();
    });
    buttons.add(cancel);
    add(buttons, BorderLayout.SOUTH);

    setSize(480, 540);
    setLocationRelativeTo(owner);
  }

  public boolean wasOkClicked() { return okClicked; }

  // ---------------------------------------------------------------------------
  // Apply helpers — collect UI values back into settings
  // ---------------------------------------------------------------------------

  private void applyAll() {
    // Global
    settings.setLumberSize(radio2x6.isSelected()
        ? BOMSettings.LumberSize.TWO_BY_SIX
        : BOMSettings.LumberSize.TWO_BY_FOUR);
    settings.setStudSpacingInches(spacing24.isSelected() ? 24 : 16);
    settings.setAllWallsFloating(allFloatingBox.isSelected());

    // Wet rooms
    List<Integer> wet = new ArrayList<>();
    for (int i = 0; i < wetRoomBoxes.size(); i++) {
      if (wetRoomBoxes.get(i).isSelected()) wet.add(i);
    }
    settings.setWetRoomIndices(wet);

    LOG.info("[BOMSettingsDialog] Applied: lumber=" + settings.getLumberSize().getDisplayName()
        + " spacing=" + settings.getStudSpacingInches() + "\""
        + " allFloating=" + settings.isAllWallsFloating()
        + " wetRooms=" + wet.size());
  }

  // ---------------------------------------------------------------------------
  // Tab builders
  // ---------------------------------------------------------------------------

  private JPanel createGlobalTab() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    GridBagConstraints gbc = baseGbc();

    // Lumber size
    gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
    panel.add(new JLabel("Lumber size (global default):"), gbc);
    gbc.gridwidth = 1;

    radio2x4 = new JRadioButton("2×4", settings.getLumberSize() == BOMSettings.LumberSize.TWO_BY_FOUR);
    radio2x6 = new JRadioButton("2×6", settings.getLumberSize() == BOMSettings.LumberSize.TWO_BY_SIX);
    ButtonGroup lgGroup = new ButtonGroup();
    lgGroup.add(radio2x4); lgGroup.add(radio2x6);

    gbc.gridx = 0; gbc.gridy = 1; panel.add(radio2x4, gbc);
    gbc.gridx = 1; panel.add(radio2x6, gbc);

    // Stud spacing
    gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
    panel.add(new JLabel("Stud spacing (global default):"), gbc);
    gbc.gridwidth = 1;

    spacing16 = new JRadioButton("16\" OC", settings.getStudSpacingInches() == 16);
    spacing24 = new JRadioButton("24\" OC", settings.getStudSpacingInches() == 24);
    ButtonGroup spGroup = new ButtonGroup();
    spGroup.add(spacing16); spGroup.add(spacing24);

    gbc.gridx = 0; gbc.gridy = 3; panel.add(spacing16, gbc);
    gbc.gridx = 1; panel.add(spacing24, gbc);

    // Wall type
    gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
    panel.add(new JLabel("Wall type (global override):"), gbc);
    gbc.gridwidth = 1;

    allFloatingBox = new JCheckBox(
        "All walls are floating (concrete slab — adds PT base plate + guide-rod nails)",
        settings.isAllWallsFloating());
    gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
    panel.add(allFloatingBox, gbc);
    gbc.gridwidth = 1;

    return wrapInScroll(panel);
  }

  private JPanel createFramingTab() {
    BOMSettings.FramingSettings fs = settings.getFraming();
    FieldPanel p = new FieldPanel();
    p.addSectionHeader("Standard lumber costs (per 8-ft board)");
    p.addField("2×4 stud cost ($):",         fs.costPerBoard2x4,  v -> fs.costPerBoard2x4  = v);
    p.addField("2×6 stud cost ($):",         fs.costPerBoard2x6,  v -> fs.costPerBoard2x6  = v);
    p.addSectionHeader("Pressure-treated costs (per 8-ft board)");
    p.addField("PT 2×4 cost ($):",           fs.costPerPTBoard2x4, v -> fs.costPerPTBoard2x4 = v);
    p.addField("PT 2×6 cost ($):",           fs.costPerPTBoard2x6, v -> fs.costPerPTBoard2x6 = v);
    p.addSectionHeader("Labor rates");
    p.addField("Labor per stud ($):",        fs.laborPerStud,     v -> fs.laborPerStud     = v);
    p.addField("Labor per lin ft plate ($):", fs.laborPerLinFtPlate, v -> fs.laborPerLinFtPlate = v);
    p.addDIYToggle(fs.isDIY, v -> fs.isDIY = v);
    return wrapInScroll(p);
  }

  private JPanel createDrywallTab() {
    BOMSettings.DrywallSettings ds = settings.getDrywall();
    FieldPanel p = new FieldPanel();
    p.addField("Cost per 4×8 sheet ($):", ds.costPerSheet,  v -> ds.costPerSheet  = v);
    p.addField("Waste factor (0–1):",      ds.wasteFactor,   v -> ds.wasteFactor   = v);
    p.addField("Labor per sheet ($):",     ds.laborPerSheet, v -> ds.laborPerSheet = v);
    p.addDIYToggle(ds.isDIY, v -> ds.isDIY = v);
    return wrapInScroll(p);
  }

  private JPanel createPaintTab() {
    BOMSettings.PaintSettings ps = settings.getPaint();
    FieldPanel p = new FieldPanel();
    p.addField("Primer cost per gallon ($):",  ps.costPerGallonPrimer, v -> ps.costPerGallonPrimer = v);
    p.addField("Finish cost per gallon ($):",  ps.costPerGallonFinish, v -> ps.costPerGallonFinish = v);
    p.addIntField("Coverage (sq ft/gallon):", ps.coverageSqFtPerGallon, v -> ps.coverageSqFtPerGallon = v);
    p.addIntField("Finish coats:",            ps.finishCoats,           v -> ps.finishCoats           = v);
    p.addField("Labor per sq ft ($):",        ps.laborPerSqFt,          v -> ps.laborPerSqFt          = v);
    p.addDIYToggle(ps.isDIY, v -> ps.isDIY = v);
    return wrapInScroll(p);
  }

  private JPanel createFlooringTab() {
    BOMSettings.FlooringSettings fs = settings.getFlooring();
    FieldPanel p = new FieldPanel();
    p.addField("Cost per sq ft ($):",  fs.costPerSqFt,   v -> fs.costPerSqFt   = v);
    p.addField("Waste factor (0–1):", fs.wasteFactor,    v -> fs.wasteFactor   = v);
    p.addField("Labor per sq ft ($):", fs.laborPerSqFt,  v -> fs.laborPerSqFt  = v);
    p.addDIYToggle(fs.isDIY, v -> fs.isDIY = v);
    return wrapInScroll(p);
  }

  private JPanel createElectricalTab() {
    BOMSettings.ElectricalSettings es = settings.getElectrical();
    FieldPanel p = new FieldPanel();
    p.addField("Base rough-in per room ($):", es.costPerRoomBase, v -> es.costPerRoomBase = v);
    p.addField("Cost per fixture ($):",       es.costPerFixture,  v -> es.costPerFixture  = v);
    p.addField("Labor per room ($):",         es.laborPerRoom,    v -> es.laborPerRoom    = v);
    p.addField("Labor per fixture ($):",      es.laborPerFixture, v -> es.laborPerFixture = v);
    p.addDIYToggle(es.isDIY, v -> es.isDIY = v);
    return wrapInScroll(p);
  }

  private JPanel createPlumbingTab() {
    BOMSettings.PlumbingSettings ps = settings.getPlumbing();
    FieldPanel p = new FieldPanel();
    p.addField("Cost per standard room ($):", ps.costPerStandardRoom,  v -> ps.costPerStandardRoom  = v);
    p.addField("Cost per wet room ($):",      ps.costPerWetRoom,       v -> ps.costPerWetRoom       = v);
    p.addField("Labor per standard room ($):", ps.laborPerStandardRoom, v -> ps.laborPerStandardRoom = v);
    p.addField("Labor per wet room ($):",     ps.laborPerWetRoom,      v -> ps.laborPerWetRoom      = v);
    p.addDIYToggle(ps.isDIY, v -> ps.isDIY = v);
    return wrapInScroll(p);
  }

  private JPanel createWetRoomsTab() {
    JPanel inner = new JPanel(new GridBagLayout());
    inner.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    GridBagConstraints gbc = baseGbc();
    gbc.gridwidth = 2;

    List<Integer> currentWet = settings.getWetRoomIndices();
    for (int i = 0; i < rooms.size(); i++) {
      Room room = rooms.get(i);
      String name = (room.getName() != null && !room.getName().isEmpty())
          ? room.getName() : "Room " + (i + 1);
      JCheckBox cb = new JCheckBox(name, currentWet.contains(i));
      wetRoomBoxes.add(cb);
      gbc.gridy = i;
      inner.add(cb, gbc);
    }

    if (rooms.isEmpty()) {
      gbc.gridy = 0;
      inner.add(new JLabel("No rooms in this plan."), gbc);
    }

    return wrapInScroll(inner);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private JPanel wrapInScroll(JPanel inner) {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.add(new JScrollPane(inner), BorderLayout.CENTER);
    return wrapper;
  }

  private GridBagConstraints baseGbc() {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets  = new Insets(4, 6, 4, 6);
    gbc.anchor  = GridBagConstraints.WEST;
    gbc.fill    = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    return gbc;
  }

  // ---------------------------------------------------------------------------
  // Inner helper for building labeled fields
  // ---------------------------------------------------------------------------

  private static class FieldPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private int row = 0;
    private final GridBagConstraints gbc;
    private final NumberFormat fmt = new DecimalFormat("0.00");

    FieldPanel() {
      super(new GridBagLayout());
      setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      gbc = new GridBagConstraints();
      gbc.insets  = new Insets(3, 6, 3, 6);
      gbc.anchor  = GridBagConstraints.WEST;
      gbc.fill    = GridBagConstraints.HORIZONTAL;
    }

    void addSectionHeader(String text) {
      gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.weightx = 1.0;
      JLabel header = new JLabel(text);
      header.setFont(header.getFont().deriveFont(Font.BOLD));
      add(header, gbc);
      gbc.gridwidth = 1;
    }

    void addField(String label, float initialValue, FloatSetter setter) {
      gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 1.0;
      add(new JLabel(label), gbc);

      JFormattedTextField field = new JFormattedTextField(fmt);
      field.setValue((double) initialValue);
      field.setColumns(9);
      field.addPropertyChangeListener("value", e -> {
        Number n = (Number) field.getValue();
        if (n != null) setter.set(n.floatValue());
      });

      gbc.gridx = 1;
      add(field, gbc);
      row++;
    }

    void addIntField(String label, int initialValue, IntSetter setter) {
      gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 1.0;
      add(new JLabel(label), gbc);

      JSpinner spinner = new JSpinner(new SpinnerNumberModel(initialValue, 1, 9999, 1));
      spinner.addChangeListener(e -> setter.set((Integer) spinner.getValue()));

      gbc.gridx = 1;
      add(spinner, gbc);
      row++;
    }

    void addDIYToggle(boolean isDIY, BoolSetter setter) {
      gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2; gbc.weightx = 1.0;
      JCheckBox cb = new JCheckBox("DIY (no labor cost)", isDIY);
      cb.addActionListener(e -> setter.set(cb.isSelected()));
      add(cb, gbc);
      gbc.gridwidth = 1;
    }
  }

  @FunctionalInterface interface FloatSetter { void set(float v); }
  @FunctionalInterface interface IntSetter   { void set(int v);   }
  @FunctionalInterface interface BoolSetter  { void set(boolean v); }
}
