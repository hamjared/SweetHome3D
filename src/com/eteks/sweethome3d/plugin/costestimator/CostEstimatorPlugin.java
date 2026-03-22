/*
 * CostEstimatorPlugin.java - Cost estimator plugin entry point
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Plugin that adds a cost estimator feature to Sweet Home 3D.
 * Allows users to estimate construction costs from their floor plans.
 */
public class CostEstimatorPlugin extends Plugin {

  @Override
  public PluginAction[] getActions() {
    try {
      System.err.println("[CostEstimatorPlugin] Loading plugin...");
      System.err.println("[CostEstimatorPlugin] Home: " + getHome());
      System.err.println("[CostEstimatorPlugin] UserPreferences: " + getUserPreferences());

      CostEstimatorAction action = new CostEstimatorAction(this);
      System.err.println("[CostEstimatorPlugin] ✓ Action created successfully");

      return new PluginAction[] { action };
    } catch (Exception e) {
      System.err.println("[CostEstimatorPlugin] ✗ ERROR loading plugin:");
      e.printStackTrace(System.err);
      return new PluginAction[0];
    }
  }

  @Override
  public void init() {
    System.err.println("[CostEstimatorPlugin] Plugin initialized");
  }
}
