/*
 * BaseboardCalculator.java - Baseboard BOM calculation
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
import com.eteks.sweethome3d.model.Wall;

/**
 * Calculates baseboard material and labor line items.
 *
 * Each wall side that has a Baseboard set contributes its wall length (in linear
 * feet) to the total.  Left and right sides are counted independently, so a wall
 * with baseboards on both sides contributes twice its length.
 */
public class BaseboardCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(BaseboardCalculator.class.getName());

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.BASEBOARD;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.BaseboardSettings bs = settings.getBaseboard();
    List<MaterialLineItem> items = new ArrayList<>();

    float totalLinFt = 0f;
    for (Wall wall : home.getWalls()) {
      float lengthFt = BOMUtil.cmToFeet(wall.getLength());
      if (wall.getLeftSideBaseboard() != null) {
        totalLinFt += lengthFt;
      }
      if (wall.getRightSideBaseboard() != null) {
        totalLinFt += lengthFt;
      }
    }

    if (totalLinFt > 0f) {
      items.add(new MaterialLineItem("Baseboard", totalLinFt, "lin ft", bs.costPerLinFt));
      if (!bs.isDIY) {
        items.add(new MaterialLineItem("Labor - baseboard install", totalLinFt, "lin ft",
            bs.laborPerLinFt, true));
      }
    }

    LOG.info("[BaseboardCalculator] totalLinFt=" + String.format("%.1f", totalLinFt)
        + " isDIY=" + bs.isDIY);
    return items;
  }
}
