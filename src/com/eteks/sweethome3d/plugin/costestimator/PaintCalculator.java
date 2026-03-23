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
 * Calculates paint material and labor line items.
 *
 * Gallons = ceil(finishedWallArea / coveragePerGallon)
 * Same surface area as drywall (10% reduction for openings applied).
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
    float sqFt = WallUtils.finishedWallAreaSqFt(home);

    int primerGal = (int) Math.ceil(sqFt / ps.coverageSqFtPerGallon);
    int finishGal = primerGal * ps.finishCoats;

    LOG.info("[PaintCalculator] area=" + String.format("%.1f", sqFt)
        + " sqft, primer=" + primerGal + " gal"
        + ", finish=" + finishGal + " gal (" + ps.finishCoats + " coat(s))"
        + ", isDIY=" + ps.isDIY);

    List<MaterialLineItem> items = new ArrayList<>();

    if (primerGal > 0) {
      items.add(new MaterialLineItem("Primer", primerGal, "gallon", ps.costPerGallonPrimer));
    }
    if (finishGal > 0) {
      items.add(new MaterialLineItem(
          "Finish coat (" + ps.finishCoats + "×)", finishGal, "gallon", ps.costPerGallonFinish));
    }

    if (!ps.isDIY && sqFt > 0) {
      items.add(new MaterialLineItem("Labor - painting", sqFt, "sq ft",
          ps.laborPerSqFt, true));
    }

    return items;
  }
}
