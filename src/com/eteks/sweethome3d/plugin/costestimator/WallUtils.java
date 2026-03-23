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

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Wall;

/**
 * Shared wall geometry utilities for stage calculators.
 */
final class WallUtils {

  private WallUtils() {}

  /**
   * Returns the total finished wall surface area in square feet, with a flat
   * 10% reduction applied to approximate door and window openings.
   *
   * Only finished sides (isLeftSideFinished / isRightSideFinished) are counted.
   * Per-wall height is used when available; falls back to home.getWallHeight().
   */
  static float finishedWallAreaSqFt(Home home) {
    float defaultHeightFt = BOMUtil.cmToFeet(home.getWallHeight());
    float rawSqFt = 0f;

    for (Wall wall : home.getWalls()) {
      float lengthFt = BOMUtil.cmToFeet(wall.getLength());
      Float heightCm = wall.getHeight();
      float heightFt = (heightCm != null) ? BOMUtil.cmToFeet(heightCm) : defaultHeightFt;
      int   sides    = (wall.isLeftSideFinished() ? 1 : 0) + (wall.isRightSideFinished() ? 1 : 0);
      rawSqFt += lengthFt * heightFt * sides;
    }

    return rawSqFt * 0.9f; // ~10% reduction for openings
  }
}
