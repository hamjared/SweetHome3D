/*
 * BOMSettings.java - Global and per-stage settings for BOM estimation
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
import java.util.ArrayList;
import java.util.List;

/**
 * All settings for BOM estimation: global (lumber size, stud spacing) and
 * per-stage (material costs, labor costs, DIY toggle).
 */
public class BOMSettings implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum LumberSize {
    TWO_BY_FOUR("2×4"),
    TWO_BY_SIX("2×6");

    private final String displayName;

    LumberSize(String displayName) {
      this.displayName = displayName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }

  // Global
  private LumberSize lumberSize = LumberSize.TWO_BY_FOUR;
  private int studSpacingInches = 16;
  /** When true, all walls are treated as floating (concrete slab) walls. */
  private boolean allWallsFloating = false;
  /** When true, wall insulation is included in the BOM. */
  private boolean wallsInsulated = false;

  // Per-stage
  private FramingSettings framing = new FramingSettings();
  private InsulationSettings insulation = new InsulationSettings();
  private DrywallSettings drywall = new DrywallSettings();
  private PaintSettings paint = new PaintSettings();
  private FlooringSettings flooring = new FlooringSettings();
  private ElectricalSettings electrical = new ElectricalSettings();
  private PlumbingSettings plumbing = new PlumbingSettings();

  // Wet rooms (carried over from previous design)
  private List<Integer> wetRoomIndices = new ArrayList<>();

  public LumberSize getLumberSize() { return lumberSize; }
  public void setLumberSize(LumberSize v) { this.lumberSize = v; }

  public int getStudSpacingInches() { return studSpacingInches; }
  public void setStudSpacingInches(int v) { this.studSpacingInches = v; }

  public boolean isAllWallsFloating() { return allWallsFloating; }
  public void setAllWallsFloating(boolean v) { this.allWallsFloating = v; }

  public boolean isWallsInsulated() { return wallsInsulated; }
  public void setWallsInsulated(boolean v) { this.wallsInsulated = v; }

  public FramingSettings getFraming() { return framing; }
  public InsulationSettings getInsulation() { return insulation; }
  public DrywallSettings getDrywall() { return drywall; }
  public PaintSettings getPaint() { return paint; }
  public FlooringSettings getFlooring() { return flooring; }
  public ElectricalSettings getElectrical() { return electrical; }
  public PlumbingSettings getPlumbing() { return plumbing; }

  public List<Integer> getWetRoomIndices() { return wetRoomIndices; }
  public void setWetRoomIndices(List<Integer> v) { this.wetRoomIndices = new ArrayList<>(v); }

  // -------------------------------------------------------------------------
  // Per-stage settings classes
  // -------------------------------------------------------------------------

  public static class InsulationSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Cost per sq ft of wall batt insulation. */
    public float costPerSqFtWall = 0.75f;
    /** Cost per sq ft of ceiling insulation (blown or batt). */
    public float costPerSqFtCeiling = 1.50f;
    /** Labor per sq ft of wall insulation installed. */
    public float laborPerSqFtWall = 0.50f;
    /** Labor per sq ft of ceiling insulation installed. */
    public float laborPerSqFtCeiling = 0.75f;
    public boolean isDIY = false;
  }

  /** Framing stage settings. Board costs are per 8-ft board; plate costs are per lin ft. */
  public static class FramingSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Cost per 8-ft 2×4 stud (used as unit cost for ea). */
    public float costPerBoard2x4 = 4.50f;
    /** Cost per 8-ft 2×6 stud. */
    public float costPerBoard2x6 = 6.50f;
    /** Cost per 8-ft pressure-treated 2×4 (floating wall base plate). */
    public float costPerPTBoard2x4 = 6.00f;
    /** Cost per 8-ft pressure-treated 2×6. */
    public float costPerPTBoard2x6 = 8.50f;
    /** Labor cost to install one stud. */
    public float laborPerStud = 2.00f;
    /** Labor cost per linear foot of plate. */
    public float laborPerLinFtPlate = 1.00f;
    public boolean isDIY = false;
  }

  public static class DrywallSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Cost per 4×8 sheet (32 sq ft). */
    public float costPerSheet = 15.00f;
    /** Waste factor as a decimal (0.10 = 10%). */
    public float wasteFactor = 0.10f;
    /** Labor cost per sheet hung. */
    public float laborPerSheet = 35.00f;
    public boolean isDIY = false;
  }

  public static class PaintSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public float costPerGallonPrimer = 25.00f;
    public float costPerGallonFinish = 35.00f;
    /** Coverage in sq ft per gallon (default 350). */
    public int coverageSqFtPerGallon = 350;
    /** Number of finish coats (default 1). */
    public int finishCoats = 1;
    /** Labor cost per sq ft painted. */
    public float laborPerSqFt = 1.50f;
    public boolean isDIY = false;
  }

  public static class FlooringSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public float costPerSqFt = 5.00f;
    /** Waste factor as a decimal (0.10 = 10%). */
    public float wasteFactor = 0.10f;
    public float laborPerSqFt = 3.00f;
    public boolean isDIY = false;
  }

  public static class ElectricalSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Base rough-in cost per room. */
    public float costPerRoomBase = 150.00f;
    /** Cost per light fixture rough-in. */
    public float costPerFixture = 100.00f;
    public float laborPerRoom = 200.00f;
    public float laborPerFixture = 50.00f;
    public boolean isDIY = false;
  }

  public static class PlumbingSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public float costPerStandardRoom = 500.00f;
    public float costPerWetRoom = 2500.00f;
    public float laborPerStandardRoom = 300.00f;
    public float laborPerWetRoom = 1500.00f;
    public boolean isDIY = false;
  }
}
