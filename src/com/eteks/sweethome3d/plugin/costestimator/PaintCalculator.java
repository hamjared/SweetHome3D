/*
 * PaintCalculator.java - Paint stage BOM calculation
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
 * Calculates paint material and labor line items for walls and ceilings.
 *
 * Walls:   primer + finish coats from finishedWallArea
 * Ceiling: finish coats only (no primer — ceilings typically get one coat of flat white)
 *          from ceilingArea (rooms with isCeilingVisible() == true)
 */
public class PaintCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(PaintCalculator.class.getName());

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.PAINT;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.PaintSettings ps = settings.getPaint();
    float wallSqFt    = WallUtils.finishedWallAreaSqFt(home);
    float ceilingSqFt = WallUtils.ceilingAreaSqFt(home);

    int wallPrimerGal  = (int) Math.ceil(wallSqFt    / ps.coverageSqFtPerGallon);
    int wallFinishGal  = wallPrimerGal * ps.finishCoats;
    int ceilFinishGal  = (int) Math.ceil(ceilingSqFt / ps.coverageSqFtPerGallon) * ps.finishCoats;

    LOG.info("[PaintCalculator] walls=" + String.format("%.1f", wallSqFt)
        + " sqft (primer=" + wallPrimerGal + " gal, finish=" + wallFinishGal + " gal)"
        + ", ceiling=" + String.format("%.1f", ceilingSqFt)
        + " sqft (finish=" + ceilFinishGal + " gal)"
        + ", isDIY=" + ps.isDIY);

    List<MaterialLineItem> items = new ArrayList<>();

    // Walls — primer + finish
    if (wallPrimerGal > 0) {
      items.add(new MaterialLineItem("Primer — walls", wallPrimerGal, "gallon",
          ps.costPerGallonPrimer));
    }
    if (wallFinishGal > 0) {
      items.add(new MaterialLineItem(
          "Finish coat — walls (" + ps.finishCoats + "×)", wallFinishGal, "gallon",
          ps.costPerGallonFinish));
    }
    if (!ps.isDIY && wallSqFt > 0) {
      items.add(new MaterialLineItem("Labor — paint walls", wallSqFt, "sq ft",
          ps.laborPerSqFt, true));
    }

    // Ceiling — finish only (no primer; one coat of flat white is standard)
    if (ceilFinishGal > 0) {
      items.add(new MaterialLineItem(
          "Ceiling paint (" + ps.finishCoats + "×)", ceilFinishGal, "gallon",
          ps.costPerGallonFinish));
    }
    if (!ps.isDIY && ceilingSqFt > 0) {
      items.add(new MaterialLineItem("Labor — paint ceiling", ceilingSqFt, "sq ft",
          ps.laborPerSqFt, true));
    }

    return items;
  }
}
