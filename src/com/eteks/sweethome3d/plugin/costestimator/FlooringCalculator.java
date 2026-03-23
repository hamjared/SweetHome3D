/*
 * FlooringCalculator.java - Flooring stage BOM calculation
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
import com.eteks.sweethome3d.model.Room;

/**
 * Calculates flooring material and labor line items.
 *
 * Effective sq ft = sum(room.getArea()) × (1 + wasteFactor)
 */
public class FlooringCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(FlooringCalculator.class.getName());

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.FLOORING;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.FlooringSettings fs = settings.getFlooring();

    float rawSqFt = 0f;
    for (Room room : home.getRooms()) {
      rawSqFt += Math.abs(room.getArea()) / BOMUtil.SQ_CM_PER_SQ_FT;
    }
    float effectiveSqFt = rawSqFt * (1f + fs.wasteFactor);

    LOG.info("[FlooringCalculator] rawArea=" + String.format("%.1f", rawSqFt)
        + " sqft, effective=" + String.format("%.1f", effectiveSqFt)
        + " sqft, isDIY=" + fs.isDIY);

    List<MaterialLineItem> items = new ArrayList<>();
    if (effectiveSqFt > 0) {
      items.add(new MaterialLineItem("Flooring", effectiveSqFt, "sq ft", fs.costPerSqFt));
      if (!fs.isDIY) {
        items.add(new MaterialLineItem("Labor - flooring install", effectiveSqFt, "sq ft",
            fs.laborPerSqFt, true));
      }
    }
    return items;
  }
}
