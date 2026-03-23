/*
 * BOMUtil.java - Shared conversion and counting helpers
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
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;

/**
 * Package-private utilities shared by all StageCalculator implementations.
 */
final class BOMUtil {

  static final float CM_PER_INCH    = 2.54f;
  static final float CM_PER_FOOT    = 12f * CM_PER_INCH;
  static final float SQ_CM_PER_SQ_FT = 929.0304f;

  private BOMUtil() {}

  static float cmToFeet(float cm)   { return cm / CM_PER_FOOT; }
  static float cmToInches(float cm) { return cm / CM_PER_INCH; }

  /** Doors = HomeDoorOrWindow with no sashes. */
  static int countDoors(Home home) {
    int n = 0;
    for (HomePieceOfFurniture p : home.getFurniture()) {
      if (p instanceof HomeDoorOrWindow && ((HomeDoorOrWindow) p).getSashes().length == 0) n++;
    }
    return n;
  }

  /** Windows = HomeDoorOrWindow with sashes. */
  static int countWindows(Home home) {
    int n = 0;
    for (HomePieceOfFurniture p : home.getFurniture()) {
      if (p instanceof HomeDoorOrWindow && ((HomeDoorOrWindow) p).getSashes().length > 0) n++;
    }
    return n;
  }

  static int countLights(Home home) {
    int n = 0;
    for (HomePieceOfFurniture p : home.getFurniture()) {
      if (p instanceof HomeLight) n++;
    }
    return n;
  }
}
