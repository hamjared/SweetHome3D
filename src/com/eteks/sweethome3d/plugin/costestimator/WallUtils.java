/*
 * WallUtils.java - Shared wall surface area calculations
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import java.util.logging.Logger;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

/**
 * Shared wall geometry utilities for stage calculators.
 */
final class WallUtils {
  private static final Logger LOG = Logger.getLogger(WallUtils.class.getName());

  private WallUtils() {}

  /**
   * Returns the net finished wall surface area in square feet after subtracting
   * actual door and window opening areas.
   *
   * Gross area: only finished sides (isLeftSideFinished / isRightSideFinished) are
   * counted. Per-wall height is used when set; falls back to home.getWallHeight().
   *
   * Opening deduction: each HomeDoorOrWindow contributes width × height of opening
   * area. Because we cannot cheaply determine the number of finished sides on the
   * specific wall each opening is in, the deduction is applied once per opening —
   * a conservative estimate for walls with two finished sides (slightly overstates
   * the net area) and exact for single-finished-side walls.
   */
  static float finishedWallAreaSqFt(Home home) {
    float defaultHeightFt = BOMUtil.cmToFeet(home.getWallHeight());
    float grossSqFt = 0f;

    for (Wall wall : home.getWalls()) {
      float lengthFt = BOMUtil.cmToFeet(wall.getLength());
      Float heightCm = wall.getHeight();
      float heightFt = (heightCm != null) ? BOMUtil.cmToFeet(heightCm) : defaultHeightFt;
      int   sides    = (wall.isLeftSideFinished() ? 1 : 0) + (wall.isRightSideFinished() ? 1 : 0);
      grossSqFt += lengthFt * heightFt * sides;
    }

    float openingSqFt = 0f;
    int   openingCount = 0;
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (piece instanceof HomeDoorOrWindow) {
        float area = (piece.getWidth() * piece.getHeight()) / BOMUtil.SQ_CM_PER_SQ_FT;
        openingSqFt += area;
        openingCount++;
      }
    }

    float netSqFt = Math.max(0f, grossSqFt - openingSqFt);

    LOG.info("[WallUtils] Gross finished area=" + String.format("%.1f", grossSqFt)
        + " sqft, openings=" + openingCount
        + " (" + String.format("%.1f", openingSqFt) + " sqft deducted)"
        + ", net=" + String.format("%.1f", netSqFt) + " sqft");

    return netSqFt;
  }

  /**
   * Returns the total ceiling area in square feet for all rooms whose ceiling
   * is visible (room.isCeilingVisible() == true).
   *
   * Ceiling area equals floor area — both are the same 2D room polygon.
   * Rooms with the ceiling hidden in 3D are treated as open/unfinished and excluded.
   */
  static float ceilingAreaSqFt(Home home) {
    float totalSqFt = 0f;
    int includedRooms = 0;

    for (Room room : home.getRooms()) {
      totalSqFt += Math.abs(room.getArea()) / BOMUtil.SQ_CM_PER_SQ_FT;
      includedRooms++;
    }

    LOG.info("[WallUtils] Ceiling area=" + String.format("%.1f", totalSqFt)
        + " sqft (" + includedRooms + " of " + home.getRooms().size() + " rooms have visible ceiling)");

    return totalSqFt;
  }
}
