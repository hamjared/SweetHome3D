/*
 * InsulationCalculator.java - Insulation stage BOM calculation
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.eteks.sweethome3d.model.Home;

/**
 * Calculates insulation material and labor line items.
 *
 * Wall insulation is included only when settings.isWallsInsulated() is true.
 * Ceiling insulation is always included (all rooms).
 *
 * Wall area counts each wall cavity once (see WallUtils.totalWallAreaSqFt).
 * Ceiling area equals total room floor area.
 */
public class InsulationCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(InsulationCalculator.class.getName());

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.INSULATION;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.InsulationSettings is = settings.getInsulation();
    List<MaterialLineItem> items = new ArrayList<>();

    if (settings.isWallsInsulated()) {
      float wallSqFt = WallUtils.totalWallAreaSqFt(home);
      LOG.info("[InsulationCalculator] Wall insulation: " + String.format("%.1f", wallSqFt)
          + " sqft @ $" + is.costPerSqFtWall + "/sqft, isDIY=" + is.isDIY);

      if (wallSqFt > 0) {
        items.add(new MaterialLineItem("Wall insulation (batt)",
            Math.round(wallSqFt), "sqft", is.costPerSqFtWall));
        if (!is.isDIY) {
          items.add(new MaterialLineItem("Labor — install wall insulation",
              Math.round(wallSqFt), "sqft", is.laborPerSqFtWall, true));
        }
      }
    } else {
      LOG.info("[InsulationCalculator] Wall insulation skipped (not insulated)");
    }

    float ceilingSqFt = WallUtils.ceilingAreaSqFt(home);
    LOG.info("[InsulationCalculator] Ceiling insulation: " + String.format("%.1f", ceilingSqFt)
        + " sqft @ $" + is.costPerSqFtCeiling + "/sqft, isDIY=" + is.isDIY);

    if (ceilingSqFt > 0) {
      items.add(new MaterialLineItem("Ceiling insulation (blown/batt)",
          Math.round(ceilingSqFt), "sqft", is.costPerSqFtCeiling));
      if (!is.isDIY) {
        items.add(new MaterialLineItem("Labor — install ceiling insulation",
            Math.round(ceilingSqFt), "sqft", is.laborPerSqFtCeiling, true));
      }
    }

    return items;
  }
}
