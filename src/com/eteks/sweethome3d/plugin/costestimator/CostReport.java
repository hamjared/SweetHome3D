/*
 * CostReport.java - Cost calculation results
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
 * Result of cost estimation calculations.
 */
public class CostReport implements Serializable {
  private static final long serialVersionUID = 1L;

  public static class LineItem implements Serializable {
    private static final long serialVersionUID = 1L;

    public String category;
    public String quantity;
    public float rate;
    public float total;
    public boolean isSubtotal;

    public LineItem(String category, String quantity, float rate, float total) {
      this(category, quantity, rate, total, false);
    }

    public LineItem(String category, String quantity, float rate, float total, boolean isSubtotal) {
      this.category = category;
      this.quantity = quantity;
      this.rate = rate;
      this.total = total;
      this.isSubtotal = isSubtotal;
    }
  }

  private LineItem[] items;
  private float grandTotal;

  public CostReport(LineItem[] items, float grandTotal) {
    this.items = items;
    this.grandTotal = grandTotal;
  }

  public LineItem[] getItems() {
    return items;
  }

  public float getGrandTotal() {
    return grandTotal;
  }
}
