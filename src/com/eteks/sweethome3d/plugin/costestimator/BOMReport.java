/*
 * BOMReport.java - BOM calculation results
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * BOM calculation result: a list of MaterialLineItems per construction stage,
 * with per-stage and grand totals.
 */
public class BOMReport implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum Stage {
    FRAMING("Framing"),
    INSULATION("Insulation"),
    DRYWALL("Drywall"),
    PAINT("Paint"),
    ELECTRICAL("Electrical"),
    PLUMBING("Plumbing"),
    FLOORING("Flooring"),
    FURNITURE("Furniture & Fixtures");

    private final String displayName;

    Stage(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  private final Map<Stage, List<MaterialLineItem>> stageItems;

  public BOMReport(Map<Stage, List<MaterialLineItem>> stageItems) {
    this.stageItems = new EnumMap<>(stageItems);
  }

  public List<MaterialLineItem> getItems(Stage stage) {
    List<MaterialLineItem> list = stageItems.get(stage);
    return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
  }

  public float getStageMaterialTotal(Stage stage) {
    float total = 0f;
    for (MaterialLineItem item : getItems(stage)) {
      total += item.getMaterialTotal();
    }
    return total;
  }

  public float getStageLaborTotal(Stage stage) {
    float total = 0f;
    for (MaterialLineItem item : getItems(stage)) {
      total += item.getLaborTotal();
    }
    return total;
  }

  public float getStageTotal(Stage stage) {
    return getStageMaterialTotal(stage) + getStageLaborTotal(stage);
  }

  public float getGrandTotal() {
    float total = 0f;
    for (Stage stage : Stage.values()) {
      total += getStageTotal(stage);
    }
    return total;
  }
}
