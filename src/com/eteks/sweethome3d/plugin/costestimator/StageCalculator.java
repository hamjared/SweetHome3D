/*
 * StageCalculator.java - Interface for per-stage BOM calculators
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import java.util.List;

import com.eteks.sweethome3d.model.Home;

/**
 * Produces the list of MaterialLineItems for one construction stage.
 * Labor appears as separate line items (isLabor=true) at the end of the list,
 * and is omitted entirely when the stage isDIY flag is set.
 */
public interface StageCalculator {

  /** The stage this calculator is responsible for. */
  BOMReport.Stage getStage();

  /**
   * Calculate all material (and, if not DIY, labor) line items for this stage.
   *
   * @param home     the SweetHome3D home model
   * @param settings current BOM settings
   * @return ordered list: material rows first, then labor rows
   */
  List<MaterialLineItem> calculate(Home home, BOMSettings settings);
}
