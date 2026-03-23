/*
 * ElectricalCalculator.java - Electrical stage BOM calculation
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
 * Calculates electrical material and labor line items.
 *
 * Base rough-in: per room.
 * Fixture rough-in: per HomeLight in the furniture list.
 */
public class ElectricalCalculator implements StageCalculator {
  private static final Logger LOG = Logger.getLogger(ElectricalCalculator.class.getName());

  @Override
  public BOMReport.Stage getStage() {
    return BOMReport.Stage.ELECTRICAL;
  }

  @Override
  public List<MaterialLineItem> calculate(Home home, BOMSettings settings) {
    BOMSettings.ElectricalSettings es = settings.getElectrical();
    int rooms  = home.getRooms().size();
    int lights = BOMUtil.countLights(home);

    LOG.info("[ElectricalCalculator] rooms=" + rooms + " lights=" + lights
        + " isDIY=" + es.isDIY);

    List<MaterialLineItem> items = new ArrayList<>();

    if (rooms > 0) {
      items.add(new MaterialLineItem("Electrical rough-in (base)", rooms, "room",
          es.costPerRoomBase));
      if (!es.isDIY) {
        items.add(new MaterialLineItem("Labor - electrical base", rooms, "room",
            es.laborPerRoom, true));
      }
    }

    if (lights > 0) {
      items.add(new MaterialLineItem("Light fixture rough-in", lights, "fixture",
          es.costPerFixture));
      if (!es.isDIY) {
        items.add(new MaterialLineItem("Labor - fixture install", lights, "fixture",
            es.laborPerFixture, true));
      }
    }

    return items;
  }
}
