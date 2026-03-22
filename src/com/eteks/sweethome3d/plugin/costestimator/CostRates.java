/*
 * CostRates.java - Cost estimator rates configuration
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
 * Configuration holder for construction cost rates.
 * All rates are in USD, quantities in appropriate units (linear feet, sq ft, count, etc).
 */
public class CostRates implements Serializable, Cloneable {
  private static final long serialVersionUID = 1L;

  // Default US rates
  private float framingPerLinearFoot = 15f;
  private float drywallPerSqFt = 2.50f;
  private float paintPerSqFt = 1.50f;
  private float flooringPerSqFt = 5.00f;
  private float electricalBasePerRoom = 150f;
  private float electricalPerFixture = 100f;
  private float plumbingPerRoom = 500f;
  private float plumbingPerWetRoom = 2500f;
  private float perDoor = 250f;
  private float perWindow = 450f;

  public CostRates() {
  }

  public CostRates(CostRates other) {
    this.framingPerLinearFoot = other.framingPerLinearFoot;
    this.drywallPerSqFt = other.drywallPerSqFt;
    this.paintPerSqFt = other.paintPerSqFt;
    this.flooringPerSqFt = other.flooringPerSqFt;
    this.electricalBasePerRoom = other.electricalBasePerRoom;
    this.electricalPerFixture = other.electricalPerFixture;
    this.plumbingPerRoom = other.plumbingPerRoom;
    this.plumbingPerWetRoom = other.plumbingPerWetRoom;
    this.perDoor = other.perDoor;
    this.perWindow = other.perWindow;
  }

  @Override
  public Object clone() {
    return new CostRates(this);
  }

  // Getters and setters
  public float getFramingPerLinearFoot() {
    return framingPerLinearFoot;
  }

  public void setFramingPerLinearFoot(float value) {
    this.framingPerLinearFoot = value;
  }

  public float getDrywallPerSqFt() {
    return drywallPerSqFt;
  }

  public void setDrywallPerSqFt(float value) {
    this.drywallPerSqFt = value;
  }

  public float getPaintPerSqFt() {
    return paintPerSqFt;
  }

  public void setPaintPerSqFt(float value) {
    this.paintPerSqFt = value;
  }

  public float getFlooringPerSqFt() {
    return flooringPerSqFt;
  }

  public void setFlooringPerSqFt(float value) {
    this.flooringPerSqFt = value;
  }

  public float getElectricalBasePerRoom() {
    return electricalBasePerRoom;
  }

  public void setElectricalBasePerRoom(float value) {
    this.electricalBasePerRoom = value;
  }

  public float getElectricalPerFixture() {
    return electricalPerFixture;
  }

  public void setElectricalPerFixture(float value) {
    this.electricalPerFixture = value;
  }

  public float getPlumbingPerRoom() {
    return plumbingPerRoom;
  }

  public void setPlumbingPerRoom(float value) {
    this.plumbingPerRoom = value;
  }

  public float getPlumbingPerWetRoom() {
    return plumbingPerWetRoom;
  }

  public void setPlumbingPerWetRoom(float value) {
    this.plumbingPerWetRoom = value;
  }

  public float getPerDoor() {
    return perDoor;
  }

  public void setPerDoor(float value) {
    this.perDoor = value;
  }

  public float getPerWindow() {
    return perWindow;
  }

  public void setPerWindow(float value) {
    this.perWindow = value;
  }
}
