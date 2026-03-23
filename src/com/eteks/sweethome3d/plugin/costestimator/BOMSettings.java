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
import java.util.Properties;

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
  // Properties-based serialization (resilient to field additions/removals)
  // -------------------------------------------------------------------------

  public Properties toProperties() {
    Properties p = new Properties();
    // Global
    p.setProperty("lumberSize",        lumberSize.name());
    p.setProperty("studSpacingInches", String.valueOf(studSpacingInches));
    p.setProperty("allWallsFloating",  String.valueOf(allWallsFloating));
    p.setProperty("wallsInsulated",    String.valueOf(wallsInsulated));
    // Framing
    p.setProperty("framing.isDIY",              String.valueOf(framing.isDIY));
    p.setProperty("framing.costPerBoard2x4",    String.valueOf(framing.costPerBoard2x4));
    p.setProperty("framing.costPerBoard2x6",    String.valueOf(framing.costPerBoard2x6));
    p.setProperty("framing.costPerPTBoard2x4",  String.valueOf(framing.costPerPTBoard2x4));
    p.setProperty("framing.costPerPTBoard2x6",  String.valueOf(framing.costPerPTBoard2x6));
    p.setProperty("framing.laborPerStud",       String.valueOf(framing.laborPerStud));
    p.setProperty("framing.laborPerLinFtPlate", String.valueOf(framing.laborPerLinFtPlate));
    // Insulation
    p.setProperty("insulation.isDIY",             String.valueOf(insulation.isDIY));
    p.setProperty("insulation.costPerSqFtWall",   String.valueOf(insulation.costPerSqFtWall));
    p.setProperty("insulation.costPerSqFtCeiling",String.valueOf(insulation.costPerSqFtCeiling));
    p.setProperty("insulation.laborPerSqFtWall",  String.valueOf(insulation.laborPerSqFtWall));
    p.setProperty("insulation.laborPerSqFtCeiling",String.valueOf(insulation.laborPerSqFtCeiling));
    // Drywall
    p.setProperty("drywall.isDIY",              String.valueOf(drywall.isDIY));
    p.setProperty("drywall.costPerSheet",       String.valueOf(drywall.costPerSheet));
    p.setProperty("drywall.wasteFactor",        String.valueOf(drywall.wasteFactor));
    p.setProperty("drywall.costPerTapeRoll",    String.valueOf(drywall.costPerTapeRoll));
    p.setProperty("drywall.sqFtPerTapeRoll",    String.valueOf(drywall.sqFtPerTapeRoll));
    p.setProperty("drywall.costPerMudBucket",   String.valueOf(drywall.costPerMudBucket));
    p.setProperty("drywall.sqFtPerMudBucket",   String.valueOf(drywall.sqFtPerMudBucket));
    p.setProperty("drywall.costPerSqFtTexture", String.valueOf(drywall.costPerSqFtTexture));
    p.setProperty("drywall.laborPerSqFt",       String.valueOf(drywall.laborPerSqFt));
    // Paint
    p.setProperty("paint.isDIY",                 String.valueOf(paint.isDIY));
    p.setProperty("paint.costPerGallonPrimer",   String.valueOf(paint.costPerGallonPrimer));
    p.setProperty("paint.costPerGallonFinish",   String.valueOf(paint.costPerGallonFinish));
    p.setProperty("paint.coverageSqFtPerGallon", String.valueOf(paint.coverageSqFtPerGallon));
    p.setProperty("paint.finishCoats",           String.valueOf(paint.finishCoats));
    p.setProperty("paint.laborPerSqFt",          String.valueOf(paint.laborPerSqFt));
    // Flooring
    p.setProperty("flooring.isDIY",                String.valueOf(flooring.isDIY));
    p.setProperty("flooring.costPerSqFtUnspecified",String.valueOf(flooring.costPerSqFtUnspecified));
    p.setProperty("flooring.costPerSqFtCarpet",     String.valueOf(flooring.costPerSqFtCarpet));
    p.setProperty("flooring.costPerSqFtTile",       String.valueOf(flooring.costPerSqFtTile));
    p.setProperty("flooring.costPerSqFtHardwood",   String.valueOf(flooring.costPerSqFtHardwood));
    p.setProperty("flooring.costPerSqFtLaminate",   String.valueOf(flooring.costPerSqFtLaminate));
    p.setProperty("flooring.wasteFactor",           String.valueOf(flooring.wasteFactor));
    p.setProperty("flooring.laborPerSqFt",          String.valueOf(flooring.laborPerSqFt));
    // Electrical
    p.setProperty("electrical.isDIY",            String.valueOf(electrical.isDIY));
    p.setProperty("electrical.costPerRoomBase",  String.valueOf(electrical.costPerRoomBase));
    p.setProperty("electrical.costPerFixture",   String.valueOf(electrical.costPerFixture));
    p.setProperty("electrical.laborPerRoom",     String.valueOf(electrical.laborPerRoom));
    p.setProperty("electrical.laborPerFixture",  String.valueOf(electrical.laborPerFixture));
    // Plumbing
    p.setProperty("plumbing.isDIY",                String.valueOf(plumbing.isDIY));
    p.setProperty("plumbing.costPerStandardRoom",  String.valueOf(plumbing.costPerStandardRoom));
    p.setProperty("plumbing.costPerWetRoom",       String.valueOf(plumbing.costPerWetRoom));
    p.setProperty("plumbing.laborPerStandardRoom", String.valueOf(plumbing.laborPerStandardRoom));
    p.setProperty("plumbing.laborPerWetRoom",      String.valueOf(plumbing.laborPerWetRoom));
    // Wet rooms
    StringBuilder wetRooms = new StringBuilder();
    for (int i = 0; i < wetRoomIndices.size(); i++) {
      if (i > 0) wetRooms.append(',');
      wetRooms.append(wetRoomIndices.get(i));
    }
    p.setProperty("wetRoomIndices", wetRooms.toString());
    return p;
  }

  public static BOMSettings fromProperties(Properties p) {
    BOMSettings s = new BOMSettings();
    // Global
    String lumberSizeVal = p.getProperty("lumberSize");
    if (lumberSizeVal != null) {
      try { s.lumberSize = LumberSize.valueOf(lumberSizeVal); } catch (IllegalArgumentException ignored) {}
    }
    s.studSpacingInches = parseInt(p, "studSpacingInches", s.studSpacingInches);
    s.allWallsFloating  = parseBool(p, "allWallsFloating",  s.allWallsFloating);
    s.wallsInsulated    = parseBool(p, "wallsInsulated",    s.wallsInsulated);
    // Framing
    s.framing.isDIY              = parseBool(p,  "framing.isDIY",              s.framing.isDIY);
    s.framing.costPerBoard2x4    = parseFloat(p, "framing.costPerBoard2x4",    s.framing.costPerBoard2x4);
    s.framing.costPerBoard2x6    = parseFloat(p, "framing.costPerBoard2x6",    s.framing.costPerBoard2x6);
    s.framing.costPerPTBoard2x4  = parseFloat(p, "framing.costPerPTBoard2x4",  s.framing.costPerPTBoard2x4);
    s.framing.costPerPTBoard2x6  = parseFloat(p, "framing.costPerPTBoard2x6",  s.framing.costPerPTBoard2x6);
    s.framing.laborPerStud       = parseFloat(p, "framing.laborPerStud",       s.framing.laborPerStud);
    s.framing.laborPerLinFtPlate = parseFloat(p, "framing.laborPerLinFtPlate", s.framing.laborPerLinFtPlate);
    // Insulation
    s.insulation.isDIY              = parseBool(p,  "insulation.isDIY",              s.insulation.isDIY);
    s.insulation.costPerSqFtWall    = parseFloat(p, "insulation.costPerSqFtWall",    s.insulation.costPerSqFtWall);
    s.insulation.costPerSqFtCeiling = parseFloat(p, "insulation.costPerSqFtCeiling", s.insulation.costPerSqFtCeiling);
    s.insulation.laborPerSqFtWall   = parseFloat(p, "insulation.laborPerSqFtWall",   s.insulation.laborPerSqFtWall);
    s.insulation.laborPerSqFtCeiling= parseFloat(p, "insulation.laborPerSqFtCeiling",s.insulation.laborPerSqFtCeiling);
    // Drywall
    s.drywall.isDIY              = parseBool(p,  "drywall.isDIY",              s.drywall.isDIY);
    s.drywall.costPerSheet       = parseFloat(p, "drywall.costPerSheet",       s.drywall.costPerSheet);
    s.drywall.wasteFactor        = parseFloat(p, "drywall.wasteFactor",        s.drywall.wasteFactor);
    s.drywall.costPerTapeRoll    = parseFloat(p, "drywall.costPerTapeRoll",    s.drywall.costPerTapeRoll);
    s.drywall.sqFtPerTapeRoll    = parseInt(p,   "drywall.sqFtPerTapeRoll",    s.drywall.sqFtPerTapeRoll);
    s.drywall.costPerMudBucket   = parseFloat(p, "drywall.costPerMudBucket",   s.drywall.costPerMudBucket);
    s.drywall.sqFtPerMudBucket   = parseInt(p,   "drywall.sqFtPerMudBucket",   s.drywall.sqFtPerMudBucket);
    s.drywall.costPerSqFtTexture = parseFloat(p, "drywall.costPerSqFtTexture", s.drywall.costPerSqFtTexture);
    s.drywall.laborPerSqFt       = parseFloat(p, "drywall.laborPerSqFt",       s.drywall.laborPerSqFt);
    // Paint
    s.paint.isDIY                = parseBool(p,  "paint.isDIY",                s.paint.isDIY);
    s.paint.costPerGallonPrimer  = parseFloat(p, "paint.costPerGallonPrimer",  s.paint.costPerGallonPrimer);
    s.paint.costPerGallonFinish  = parseFloat(p, "paint.costPerGallonFinish",  s.paint.costPerGallonFinish);
    s.paint.coverageSqFtPerGallon= parseInt(p,   "paint.coverageSqFtPerGallon",s.paint.coverageSqFtPerGallon);
    s.paint.finishCoats          = parseInt(p,   "paint.finishCoats",          s.paint.finishCoats);
    s.paint.laborPerSqFt         = parseFloat(p, "paint.laborPerSqFt",         s.paint.laborPerSqFt);
    // Flooring
    s.flooring.isDIY                = parseBool(p,  "flooring.isDIY",                s.flooring.isDIY);
    s.flooring.costPerSqFtUnspecified=parseFloat(p, "flooring.costPerSqFtUnspecified",s.flooring.costPerSqFtUnspecified);
    s.flooring.costPerSqFtCarpet    = parseFloat(p, "flooring.costPerSqFtCarpet",    s.flooring.costPerSqFtCarpet);
    s.flooring.costPerSqFtTile      = parseFloat(p, "flooring.costPerSqFtTile",      s.flooring.costPerSqFtTile);
    s.flooring.costPerSqFtHardwood  = parseFloat(p, "flooring.costPerSqFtHardwood",  s.flooring.costPerSqFtHardwood);
    s.flooring.costPerSqFtLaminate  = parseFloat(p, "flooring.costPerSqFtLaminate",  s.flooring.costPerSqFtLaminate);
    s.flooring.wasteFactor          = parseFloat(p, "flooring.wasteFactor",          s.flooring.wasteFactor);
    s.flooring.laborPerSqFt         = parseFloat(p, "flooring.laborPerSqFt",         s.flooring.laborPerSqFt);
    // Electrical
    s.electrical.isDIY           = parseBool(p,  "electrical.isDIY",           s.electrical.isDIY);
    s.electrical.costPerRoomBase = parseFloat(p, "electrical.costPerRoomBase", s.electrical.costPerRoomBase);
    s.electrical.costPerFixture  = parseFloat(p, "electrical.costPerFixture",  s.electrical.costPerFixture);
    s.electrical.laborPerRoom    = parseFloat(p, "electrical.laborPerRoom",    s.electrical.laborPerRoom);
    s.electrical.laborPerFixture = parseFloat(p, "electrical.laborPerFixture", s.electrical.laborPerFixture);
    // Plumbing
    s.plumbing.isDIY                = parseBool(p,  "plumbing.isDIY",                s.plumbing.isDIY);
    s.plumbing.costPerStandardRoom  = parseFloat(p, "plumbing.costPerStandardRoom",  s.plumbing.costPerStandardRoom);
    s.plumbing.costPerWetRoom       = parseFloat(p, "plumbing.costPerWetRoom",       s.plumbing.costPerWetRoom);
    s.plumbing.laborPerStandardRoom = parseFloat(p, "plumbing.laborPerStandardRoom", s.plumbing.laborPerStandardRoom);
    s.plumbing.laborPerWetRoom      = parseFloat(p, "plumbing.laborPerWetRoom",      s.plumbing.laborPerWetRoom);
    // Wet rooms
    String wetVal = p.getProperty("wetRoomIndices", "").trim();
    if (!wetVal.isEmpty()) {
      for (String part : wetVal.split(",")) {
        try { s.wetRoomIndices.add(Integer.parseInt(part.trim())); } catch (NumberFormatException ignored) {}
      }
    }
    return s;
  }

  private static boolean parseBool(Properties p, String key, boolean defaultVal) {
    String v = p.getProperty(key);
    return v != null ? Boolean.parseBoolean(v) : defaultVal;
  }

  private static float parseFloat(Properties p, String key, float defaultVal) {
    String v = p.getProperty(key);
    if (v == null) return defaultVal;
    try { return Float.parseFloat(v); } catch (NumberFormatException e) { return defaultVal; }
  }

  private static int parseInt(Properties p, String key, int defaultVal) {
    String v = p.getProperty(key);
    if (v == null) return defaultVal;
    try { return Integer.parseInt(v); } catch (NumberFormatException e) { return defaultVal; }
  }

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
    /** Cost per roll of drywall tape. */
    public float costPerTapeRoll = 8.00f;
    /** Finished sq ft covered per roll of tape. */
    public int sqFtPerTapeRoll = 200;
    /** Cost per bucket of joint compound (4.5 gal). */
    public float costPerMudBucket = 20.00f;
    /** Finished sq ft covered per bucket of mud. */
    public int sqFtPerMudBucket = 100;
    /** Texture material cost per sq ft. */
    public float costPerSqFtTexture = 0.15f;
    /** Combined labor cost per sq ft (hang, tape, mud, and texture). */
    public float laborPerSqFt = 2.50f;
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

    /** Material cost per sq ft for rooms with no flooring type set. */
    public float costPerSqFtUnspecified = 5.00f;
    public float costPerSqFtCarpet      = 3.50f;
    public float costPerSqFtTile        = 5.00f;
    public float costPerSqFtHardwood    = 8.00f;
    public float costPerSqFtLaminate    = 4.00f;
    /** Waste factor as a decimal (0.10 = 10%). Applied to all types. */
    public float wasteFactor = 0.10f;
    public float laborPerSqFt = 3.00f;
    public boolean isDIY = false;

    public float getCostForType(com.eteks.sweethome3d.model.FlooringType type) {
      switch (type) {
        case CARPET:   return costPerSqFtCarpet;
        case TILE:     return costPerSqFtTile;
        case HARDWOOD: return costPerSqFtHardwood;
        case LAMINATE: return costPerSqFtLaminate;
        default:       return costPerSqFtUnspecified;
      }
    }
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
