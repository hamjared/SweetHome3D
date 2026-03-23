/*
 * FramingCalculator.java - Framing stage BOM calculation
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
 * Calculates framing material and labor line items.
 *
 * Standard wall:  bottom plate + top plate + studs + king/jack studs + headers
 * Floating wall:  PT bottom base plate + standard top base plate + top plate + studs + nails
 *                 (activated when wall.isFloating() stub becomes real)
 */
public class FramingCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(FramingCalculator.class.getName());

  /** Floating wall: 60d guide rod nails spaced every 34 inches. */
  private static final float FLOATING_NAIL_SPACING_IN = 34f;

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.FRAMING;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.FramingSettings fs = settings.getFraming();
    boolean use2x6    = settings.getLumberSize() == BOMSettings.LumberSize.TWO_BY_SIX;
    int     spacingIn = settings.getStudSpacingInches();
    String  sizeName  = use2x6 ? "2×6" : "2×4";

    float studCost       = use2x6 ? fs.costPerBoard2x6    : fs.costPerBoard2x4;
    float plateCostPerFt = (use2x6 ? fs.costPerBoard2x6   : fs.costPerBoard2x4)   / 8f;
    float ptCostPerFt    = (use2x6 ? fs.costPerPTBoard2x6 : fs.costPerPTBoard2x4) / 8f;

    int   totalStuds          = 0;
    float totalTopPlateFt     = 0f;
    float totalStdBottomFt    = 0f;
    float totalPTBottomFt     = 0f;
    int   totalGuideRodNails  = 0;

    LOG.info("[FramingCalculator] " + home.getWalls().size() + " walls"
        + ", lumber=" + sizeName
        + ", spacing=" + spacingIn + "\""
        + ", allFloating=" + settings.isAllWallsFloating());

    for (Wall wall : home.getWalls()) {
      float lengthIn = BOMUtil.cmToInches(wall.getLength());
      float lengthFt = BOMUtil.cmToFeet(wall.getLength());

      // Per-wall stub falls back to global setting; doubleTop stub until model property lands
      boolean floating  = settings.isAllWallsFloating(); // wall.isFloating() when model flag lands
      boolean doubleTop = false;                          // wall.hasDoubleTopPlate() stub

      int studs = (int) (lengthIn / spacingIn) + 1;
      totalStuds += studs;

      if (floating) {
        totalPTBottomFt  += lengthFt;                       // PT base plate (contacts slab)
        totalStdBottomFt += lengthFt;                       // top base plate (studs attach here)
        totalTopPlateFt  += lengthFt * (doubleTop ? 2 : 1);
        int nails = (int) (lengthIn / FLOATING_NAIL_SPACING_IN);
        totalGuideRodNails += nails;
        LOG.fine("[FramingCalculator] Floating wall: " + String.format("%.1f", lengthFt)
            + " ft, studs=" + studs + ", nails=" + nails);
      } else {
        totalStdBottomFt += lengthFt;
        totalTopPlateFt  += lengthFt * (doubleTop ? 2 : 1);
        LOG.fine("[FramingCalculator] Standard wall: " + String.format("%.1f", lengthFt)
            + " ft, studs=" + studs);
      }
    }

    int doorCount   = BOMUtil.countDoors(home);
    int windowCount = BOMUtil.countWindows(home);
    int kingJack    = 2 * doorCount + 2 * windowCount;
    int headers     = doorCount + windowCount;

    LOG.info("[FramingCalculator] Totals: studs=" + totalStuds
        + " topPlate=" + String.format("%.1f", totalTopPlateFt) + " ft"
        + " stdBottom=" + String.format("%.1f", totalStdBottomFt) + " ft"
        + " PTBottom=" + String.format("%.1f", totalPTBottomFt) + " ft"
        + " headers=" + headers + " kingJack=" + kingJack
        + " guideNails=" + totalGuideRodNails
        + " isDIY=" + fs.isDIY);

    // ── Material items ──────────────────────────────────────────────────────
    List<MaterialLineItem> mat   = new ArrayList<>();
    List<MaterialLineItem> labor = new ArrayList<>();

    if (totalStuds > 0) {
      mat.add(new MaterialLineItem(sizeName + " stud", totalStuds, "ea", studCost));

      if (!fs.isDIY) {
        labor.add(new MaterialLineItem("Labor - stud install", totalStuds, "ea",
            fs.laborPerStud, true));
      }
    }

    if (totalPTBottomFt > 0) {
      mat.add(new MaterialLineItem("Bottom base plate (PT " + sizeName + ")",
          totalPTBottomFt, "lin ft", ptCostPerFt));
      mat.add(new MaterialLineItem("Top base plate (" + sizeName + ")",
          totalStdBottomFt, "lin ft", plateCostPerFt));

      if (!fs.isDIY) {
        double totalPlatesFt = totalPTBottomFt + totalStdBottomFt;
        labor.add(new MaterialLineItem("Labor - base plates", totalPlatesFt, "lin ft",
            fs.laborPerLinFtPlate, true));
      }
    } else if (totalStdBottomFt > 0) {
      mat.add(new MaterialLineItem("Bottom plate (" + sizeName + ")",
          totalStdBottomFt, "lin ft", plateCostPerFt));

      if (!fs.isDIY) {
        labor.add(new MaterialLineItem("Labor - bottom plate", totalStdBottomFt, "lin ft",
            fs.laborPerLinFtPlate, true));
      }
    }

    if (totalTopPlateFt > 0) {
      mat.add(new MaterialLineItem("Top plate (" + sizeName + ")",
          totalTopPlateFt, "lin ft", plateCostPerFt));

      if (!fs.isDIY) {
        labor.add(new MaterialLineItem("Labor - top plate", totalTopPlateFt, "lin ft",
            fs.laborPerLinFtPlate, true));
      }
    }

    if (kingJack > 0) {
      mat.add(new MaterialLineItem("King/jack studs (" + sizeName + ")",
          kingJack, "ea", studCost));

      if (!fs.isDIY) {
        labor.add(new MaterialLineItem("Labor - king/jack studs", kingJack, "ea",
            fs.laborPerStud, true));
      }
    }

    if (headers > 0) {
      mat.add(new MaterialLineItem("Header (" + sizeName + ")",
          headers, "ea", studCost * 1.5f));

      if (!fs.isDIY) {
        labor.add(new MaterialLineItem("Labor - headers", headers, "ea",
            fs.laborPerStud * 2f, true));
      }
    }

    if (totalGuideRodNails > 0) {
      mat.add(new MaterialLineItem("60d guide rod nails", totalGuideRodNails, "ea", 0.50f));
      // Nails are included in general labor; no separate labor line
    }

    mat.addAll(labor);
    return mat;
  }
}
