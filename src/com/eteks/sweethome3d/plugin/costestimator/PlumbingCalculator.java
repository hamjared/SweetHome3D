/*
 * PlumbingCalculator.java - Plumbing stage BOM calculation
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
 * Calculates plumbing material and labor line items.
 *
 * Wet rooms (bathroom, laundry, etc.) are flagged in BOMSettings and carry
 * a higher per-room rate than standard rooms.
 */
public class PlumbingCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(PlumbingCalculator.class.getName());

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.PLUMBING;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.PlumbingSettings ps = settings.getPlumbing();
    int totalRooms = home.getRooms().size();
    int wetRooms   = settings.getWetRoomIndices().size();
    int stdRooms   = totalRooms - wetRooms;

    LOG.info("[PlumbingCalculator] standardRooms=" + stdRooms + " wetRooms=" + wetRooms
        + " isDIY=" + ps.isDIY);

    List<MaterialLineItem> items = new ArrayList<>();

    if (stdRooms > 0) {
      items.add(new MaterialLineItem("Plumbing (standard room)", stdRooms, "room",
          ps.costPerStandardRoom));
      if (!ps.isDIY) {
        items.add(new MaterialLineItem("Labor - plumbing (standard)", stdRooms, "room",
            ps.laborPerStandardRoom, true));
      }
    }

    if (wetRooms > 0) {
      items.add(new MaterialLineItem("Plumbing (wet room)", wetRooms, "room",
          ps.costPerWetRoom));
      if (!ps.isDIY) {
        items.add(new MaterialLineItem("Labor - plumbing (wet)", wetRooms, "room",
            ps.laborPerWetRoom, true));
      }
    }

    return items;
  }
}
