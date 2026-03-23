/*
 * BOMCalculator.java - BOM orchestrator
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.eteks.sweethome3d.model.Home;

/**
 * Orchestrates all StageCalculators and assembles the final BOMReport.
 * Each stage's calculation logic lives in its own StageCalculator implementation.
 */
public class BOMCalculator {
  private static final Logger LOG = Logger.getLogger(BOMCalculator.class.getName());

  private static final StageCalculator[] CALCULATORS = {
    new FramingCalculator(),
    new InsulationCalculator(),
    new DrywallCalculator(),
    new PaintCalculator(),
    new ElectricalCalculator(),
    new PlumbingCalculator(),
    new FlooringCalculator(),
  };

  private BOMCalculator() {}

  public static BOMReport calculate(Home home, BOMSettings settings) {
    LOG.info("[BOMCalculator] Starting BOM calculation"
        + " — walls=" + home.getWalls().size()
        + " rooms=" + home.getRooms().size()
        + " furniture=" + home.getFurniture().size()
        + " lumber=" + settings.getLumberSize().getDisplayName()
        + " spacing=" + settings.getStudSpacingInches() + "\"");

    Map<BOMReport.Stage, List<MaterialLineItem>> stageItems =
        new EnumMap<>(BOMReport.Stage.class);

    for (StageCalculator calc : CALCULATORS) {
      List<MaterialLineItem> items = calc.calculate(home, settings);
      stageItems.put(calc.getStage(), items);
      LOG.fine("[BOMCalculator] " + calc.getStage().getDisplayName()
          + " → " + items.size() + " line items");
    }

    BOMReport report = new BOMReport(stageItems);
    LOG.info("[BOMCalculator] Grand total: $" + String.format("%.2f", report.getGrandTotal()));
    return report;
  }
}
