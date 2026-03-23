/*
 * DrywallCalculator.java - Drywall stage BOM calculation
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
 * Calculates drywall material and labor line items.
 *
 * Sheet count = ceil(finishedWallArea × (1 + wasteFactor) / 32)
 * Opening area is approximated as a flat 10% reduction.
 */
public class DrywallCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(DrywallCalculator.class.getName());
  private static final float SHEET_SQ_FT = 32f; // 4×8 sheet

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.DRYWALL;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.DrywallSettings ds = settings.getDrywall();
    float rawSqFt = WallUtils.finishedWallAreaSqFt(home);
    int sheets = (int) Math.ceil(rawSqFt * (1f + ds.wasteFactor) / SHEET_SQ_FT);

    LOG.info("[DrywallCalculator] rawArea=" + String.format("%.1f", rawSqFt)
        + " sqft, waste=" + (int)(ds.wasteFactor * 100) + "%, sheets=" + sheets
        + ", isDIY=" + ds.isDIY);

    List<MaterialLineItem> items = new ArrayList<>();
    if (sheets > 0) {
      items.add(new MaterialLineItem("Drywall sheet (4×8)", sheets, "sheet", ds.costPerSheet));
      if (!ds.isDIY) {
        items.add(new MaterialLineItem("Labor - hang drywall", sheets, "sheet",
            ds.laborPerSheet, true));
      }
    }
    return items;
  }
}
