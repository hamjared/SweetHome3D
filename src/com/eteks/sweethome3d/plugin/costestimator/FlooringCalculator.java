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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.eteks.sweethome3d.model.FlooringType;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Room;

/**
 * Calculates flooring material and labor line items, broken down by flooring type.
 *
 * Effective sq ft per type = rawSqFt × (1 + wasteFactor)
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

    // Bucket raw sq ft by flooring type (preserving enum order)
    Map<FlooringType, Float> rawByType = new LinkedHashMap<>();
    for (FlooringType type : FlooringType.values()) {
      rawByType.put(type, 0f);
    }
    for (Room room : home.getRooms()) {
      FlooringType type = room.getFlooringType();
      rawByType.put(type, rawByType.get(type) + Math.abs(room.getArea()) / BOMUtil.SQ_CM_PER_SQ_FT);
    }

    List<MaterialLineItem> items = new ArrayList<>();
    float totalEffectiveSqFt = 0f;

    for (Map.Entry<FlooringType, Float> entry : rawByType.entrySet()) {
      float rawSqFt = entry.getValue();
      if (rawSqFt <= 0f) continue;
      FlooringType type = entry.getKey();
      float effectiveSqFt = rawSqFt * (1f + fs.wasteFactor);
      totalEffectiveSqFt += effectiveSqFt;
      float cost = fs.getCostForType(type);
      LOG.info("[FlooringCalculator] " + type.getDisplayName()
          + " raw=" + String.format("%.1f", rawSqFt)
          + " effective=" + String.format("%.1f", effectiveSqFt)
          + " $/sqft=" + cost);
      items.add(new MaterialLineItem(type.getDisplayName() + " flooring", effectiveSqFt, "sq ft", cost));
    }

    if (!fs.isDIY && totalEffectiveSqFt > 0f) {
      items.add(new MaterialLineItem("Labor - flooring install", totalEffectiveSqFt, "sq ft",
          fs.laborPerSqFt, true));
    }

    LOG.info("[FlooringCalculator] totalEffective=" + String.format("%.1f", totalEffectiveSqFt)
        + " isDIY=" + fs.isDIY);
    return items;
  }
}
