/*
 * CostCalculator.java - Pure cost calculation logic
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
import java.util.Collection;
import java.util.List;

import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;

/**
 * Calculates construction costs from home data.
 */
public class CostCalculator {
  private static final float CM_PER_INCH = 2.54f;
  private static final float CM_PER_FOOT = 12f * CM_PER_INCH;

  private CostCalculator() {
  }

  /**
   * Calculates cost report from home with specified rates and wet room flags.
   */
  public static CostReport calculate(Home home, CostRates rates, List<Integer> wetRoomIndices) {
    List<CostReport.LineItem> items = new ArrayList<>();
    float grandTotal = 0f;

    // Walls (framing)
    float wallLinearFeet = calculateWallLinearFeet(home);
    float framingTotal = wallLinearFeet * rates.getFramingPerLinearFoot();
    items.add(new CostReport.LineItem("Framing", String.format("%.0f ft", wallLinearFeet),
        rates.getFramingPerLinearFoot(), framingTotal));
    grandTotal += framingTotal;

    // Wall surface (drywall, both sides)
    float wallSurfaceSqFt = calculateWallSurfaceSqFt(home);
    float drywallTotal = wallSurfaceSqFt * rates.getDrywallPerSqFt();
    items.add(new CostReport.LineItem("Drywall", String.format("%.0f sq ft", wallSurfaceSqFt),
        rates.getDrywallPerSqFt(), drywallTotal));
    grandTotal += drywallTotal;

    // Paint (same surface as drywall)
    float paintTotal = wallSurfaceSqFt * rates.getPaintPerSqFt();
    items.add(new CostReport.LineItem("Paint", String.format("%.0f sq ft", wallSurfaceSqFt),
        rates.getPaintPerSqFt(), paintTotal));
    grandTotal += paintTotal;

    // Flooring
    float floorAreaSqFt = calculateFloorAreaSqFt(home);
    float flooringTotal = floorAreaSqFt * rates.getFlooringPerSqFt();
    items.add(new CostReport.LineItem("Flooring", String.format("%.0f sq ft", floorAreaSqFt),
        rates.getFlooringPerSqFt(), flooringTotal));
    grandTotal += flooringTotal;

    // Electrical
    int roomCount = home.getRooms().size();
    int lightCount = countLights(home);
    float electricalBaseTotal = roomCount * rates.getElectricalBasePerRoom();
    float electricalFixtureTotal = lightCount * rates.getElectricalPerFixture();
    float electricalTotal = electricalBaseTotal + electricalFixtureTotal;
    items.add(new CostReport.LineItem("Electrical (base)", String.format("%d rooms", roomCount),
        rates.getElectricalBasePerRoom(), electricalBaseTotal));
    items.add(new CostReport.LineItem("Electrical (fixtures)", String.format("%d lights", lightCount),
        rates.getElectricalPerFixture(), electricalFixtureTotal));
    grandTotal += electricalTotal;

    // Plumbing
    int standardRooms = roomCount - (wetRoomIndices != null ? wetRoomIndices.size() : 0);
    int wetRooms = wetRoomIndices != null ? wetRoomIndices.size() : 0;
    float plumbingStandardTotal = standardRooms * rates.getPlumbingPerRoom();
    float plumbingWetTotal = wetRooms * rates.getPlumbingPerWetRoom();
    float plumbingTotal = plumbingStandardTotal + plumbingWetTotal;
    if (standardRooms > 0) {
      items.add(new CostReport.LineItem("Plumbing (standard)", String.format("%d rooms", standardRooms),
          rates.getPlumbingPerRoom(), plumbingStandardTotal));
    }
    if (wetRooms > 0) {
      items.add(new CostReport.LineItem("Plumbing (wet rooms)", String.format("%d rooms", wetRooms),
          rates.getPlumbingPerWetRoom(), plumbingWetTotal));
    }
    grandTotal += plumbingTotal;

    // Doors and windows
    int doorCount = countDoors(home);
    int windowCount = countWindows(home);
    float doorsTotal = doorCount * rates.getPerDoor();
    float windowsTotal = windowCount * rates.getPerWindow();
    items.add(new CostReport.LineItem("Doors", String.format("%d doors", doorCount),
        rates.getPerDoor(), doorsTotal));
    items.add(new CostReport.LineItem("Windows", String.format("%d windows", windowCount),
        rates.getPerWindow(), windowsTotal));
    grandTotal += doorsTotal + windowsTotal;

    // Furniture (sum of prices)
    float furnitureTotal = calculateFurnitureTotal(home);
    if (furnitureTotal > 0) {
      items.add(new CostReport.LineItem("Furniture", "(items)", 0f, furnitureTotal));
      grandTotal += furnitureTotal;
    }

    CostReport.LineItem[] itemArray = items.toArray(new CostReport.LineItem[0]);
    return new CostReport(itemArray, grandTotal);
  }

  private static float calculateWallLinearFeet(Home home) {
    float totalCm = 0f;
    for (Wall wall : home.getWalls()) {
      totalCm += wall.getLength();
    }
    return cmToFeet(totalCm);
  }

  private static float calculateWallSurfaceSqFt(Home home) {
    // Sum wall surface area based on per-wall drywall configuration
    // For simplicity, use default wall height and apply a rough opening reduction factor
    float wallHeight = cmToFeet(home.getWallHeight());
    float totalSurfaceArea = 0f;

    for (Wall wall : home.getWalls()) {
      float wallLengthFt = cmToFeet(wall.getLength());
      float wallArea = wallLengthFt * wallHeight;

      int finishedSides = (wall.isLeftSideFinished() ? 1 : 0) + (wall.isRightSideFinished() ? 1 : 0);
      totalSurfaceArea += wallArea * finishedSides;
    }

    // Reduce by ~10% for door/window openings
    return totalSurfaceArea * 0.9f;
  }

  private static float calculateWallPerimeter(Home home) {
    float totalCm = 0f;
    for (Wall wall : home.getWalls()) {
      totalCm += wall.getLength();
    }
    return totalCm;
  }

  private static float calculateFloorAreaSqFt(Home home) {
    float totalAreaCm2 = 0f;
    for (Room room : home.getRooms()) {
      totalAreaCm2 += Math.abs(room.getArea());
    }
    // Convert cm² to ft² (1 ft = 30.48 cm, so 1 ft² = 929.03 cm²)
    return totalAreaCm2 / 929.03f;
  }

  private static int countDoors(Home home) {
    int count = 0;
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (piece instanceof HomeDoorOrWindow) {
        // Windows have sashes, doors typically don't
        HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow) piece;
        if (doorOrWindow.getSashes().length == 0) {
          count++;
        }
      }
    }
    return count;
  }

  private static int countWindows(Home home) {
    int count = 0;
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (piece instanceof HomeDoorOrWindow) {
        // Windows have sashes, doors typically don't
        HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow) piece;
        if (doorOrWindow.getSashes().length > 0) {
          count++;
        }
      }
    }
    return count;
  }

  private static int countLights(Home home) {
    int count = 0;
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (piece instanceof HomeLight) {
        count++;
      }
    }
    return count;
  }

  private static float calculateFurnitureTotal(Home home) {
    float total = 0f;
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      if (!(piece instanceof HomeDoorOrWindow) && !(piece instanceof HomeLight)) {
        Object priceObj = piece.getPriceValueAddedTaxIncluded();
        if (priceObj != null) {
          try {
            // Handle BigDecimal and Number types
            if (priceObj instanceof Number) {
              float price = ((Number) priceObj).floatValue();
              if (price > 0) {
                total += price;
              }
            }
          } catch (Exception e) {
            // Ignore conversion errors
          }
        }
      }
    }
    return total;
  }

  private static float cmToFeet(float cm) {
    return cm / CM_PER_FOOT;
  }
}
