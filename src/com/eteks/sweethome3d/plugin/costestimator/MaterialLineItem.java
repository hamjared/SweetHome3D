/*
 * MaterialLineItem.java - One BOM row
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

/**
 * One row in the BOM table.  Labor appears as its own rows (isLabor=true)
 * rather than as a separate column alongside every material.
 *
 * Total = quantity × unitCost
 */
public class MaterialLineItem implements Serializable {
  private static final long serialVersionUID = 1L;

  private final String  name;
  private final double  quantity;
  private final String  unit;       // "ea", "lin ft", "sq ft", "sheet", "gallon"
  private final float   unitCost;
  private final boolean isLabor;

  /** Material item (isLabor=false). */
  public MaterialLineItem(String name, double quantity, String unit, float unitCost) {
    this(name, quantity, unit, unitCost, false);
  }

  /** General constructor; isLabor=true marks this as a labor row. */
  public MaterialLineItem(String name, double quantity, String unit, float unitCost, boolean isLabor) {
    this.name     = name;
    this.quantity = quantity;
    this.unit     = unit;
    this.unitCost = unitCost;
    this.isLabor  = isLabor;
  }

  public String  getName()     { return name; }
  public double  getQuantity() { return quantity; }
  public String  getUnit()     { return unit; }
  public float   getUnitCost() { return unitCost; }
  public boolean isLabor()     { return isLabor; }

  public float getTotal()         { return (float) (quantity * unitCost); }
  public float getMaterialTotal() { return isLabor ? 0f : getTotal(); }
  public float getLaborTotal()    { return isLabor ? getTotal() : 0f; }
}
