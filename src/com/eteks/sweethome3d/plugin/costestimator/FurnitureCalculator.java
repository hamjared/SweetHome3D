/*
 * FurnitureCalculator.java - Furniture & fixtures BOM calculation
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;

/**
 * Calculates furniture and fixture line items from the cost set on each piece.
 *
 * Only pieces with a non-null cost are included.  When a piece has no name,
 * "Unnamed piece" is used as the label.
 */
public class FurnitureCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(FurnitureCalculator.class.getName());

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.FURNITURE;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.FurnitureSettings fs = settings.getFurniture();
    List<MaterialLineItem> items = new ArrayList<>();
    int pieceCount = 0;

    for (HomePieceOfFurniture piece : home.getFurniture()) {
      BigDecimal cost = piece.getCost();
      if (cost == null) continue;
      String name = (piece.getName() != null && !piece.getName().isEmpty())
          ? piece.getName() : "Unnamed piece";
      items.add(new MaterialLineItem(name, 1f, "ea", cost.floatValue()));
      pieceCount++;
    }

    if (!fs.isDIY && fs.laborPerPiece > 0f && pieceCount > 0) {
      items.add(new MaterialLineItem("Labor - furniture assembly/install",
          pieceCount, "ea", fs.laborPerPiece, true));
    }

    LOG.info("[FurnitureCalculator] pieces with cost=" + pieceCount + " isDIY=" + fs.isDIY);
    return items;
  }
}
