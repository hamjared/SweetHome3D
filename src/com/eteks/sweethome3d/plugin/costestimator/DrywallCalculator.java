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
 * Calculates drywall material and labor line items for walls and ceilings.
 *
 * Wall sheets  = ceil(finishedWallArea × (1 + wasteFactor) / 32)
 * Ceiling sheets = ceil(ceilingArea × (1 + wasteFactor) / 32)
 * Ceiling area equals floor area for rooms with isCeilingVisible() == true.
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
    float wallSqFt    = WallUtils.finishedWallAreaSqFt(home);
    float ceilingSqFt = WallUtils.ceilingAreaSqFt(home);
    float finishSqFt  = wallSqFt + ceilingSqFt;
    int wallSheets    = (int) Math.ceil(wallSqFt    * (1f + ds.wasteFactor) / SHEET_SQ_FT);
    int ceilingSheets = (int) Math.ceil(ceilingSqFt * (1f + ds.wasteFactor) / SHEET_SQ_FT);
    int tapeRolls     = (ds.sqFtPerTapeRoll > 0 && finishSqFt > 0)
        ? (int) Math.ceil(finishSqFt / ds.sqFtPerTapeRoll) : 0;
    int mudBuckets    = (ds.sqFtPerMudBucket > 0 && finishSqFt > 0)
        ? (int) Math.ceil(finishSqFt / ds.sqFtPerMudBucket) : 0;

    LOG.info("[DrywallCalculator] walls=" + String.format("%.1f", wallSqFt)
        + " sqft (" + wallSheets + " sheets)"
        + ", ceiling=" + String.format("%.1f", ceilingSqFt)
        + " sqft (" + ceilingSheets + " sheets)"
        + ", finish=" + String.format("%.1f", finishSqFt) + " sqft"
        + ", tape=" + tapeRolls + " rolls, mud=" + mudBuckets + " buckets"
        + ", waste=" + (int)(ds.wasteFactor * 100) + "%"
        + ", isDIY=" + ds.isDIY);

    List<MaterialLineItem> items = new ArrayList<>();

    if (wallSheets > 0) {
      items.add(new MaterialLineItem("Drywall sheet (4×8) — walls", wallSheets, "sheet",
          ds.costPerSheet));
    }

    if (ceilingSheets > 0) {
      items.add(new MaterialLineItem("Drywall sheet (4×8) — ceiling", ceilingSheets, "sheet",
          ds.costPerSheet));
    }

    if (tapeRolls > 0) {
      items.add(new MaterialLineItem("Drywall tape", tapeRolls, "roll", ds.costPerTapeRoll));
    }

    if (mudBuckets > 0) {
      items.add(new MaterialLineItem("Joint compound (4.5 gal)", mudBuckets, "bucket",
          ds.costPerMudBucket));
    }

    if (finishSqFt > 0) {
      items.add(new MaterialLineItem("Texture", (int) Math.ceil(finishSqFt), "sqft",
          ds.costPerSqFtTexture));
      if (!ds.isDIY) {
        items.add(new MaterialLineItem("Labor — hang, tape, mud & texture",
            (int) Math.ceil(finishSqFt), "sqft", ds.laborPerSqFt, true));
      }
    }

    return items;
  }
}
