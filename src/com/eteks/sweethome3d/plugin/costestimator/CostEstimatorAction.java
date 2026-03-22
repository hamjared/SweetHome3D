/*
 * CostEstimatorAction.java - Cost estimator menu action
 *
 * Sweet Home 3D, Copyright (c) 2024 Space Mushrooms <info@sweethome3d.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 */
package com.eteks.sweethome3d.plugin.costestimator;

import javax.swing.SwingUtilities;

import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;

/**
 * Action that opens the cost estimator dialog.
 */
public class CostEstimatorAction extends PluginAction {
  private Plugin plugin;

  public CostEstimatorAction(Plugin plugin) {
    super("com.eteks.sweethome3d.plugin.costestimator.CostEstimatorPlugin",
        "SHOW_COST_ESTIMATOR", plugin.getPluginClassLoader(), true);
    this.plugin = plugin;
  }

  @Override
  public void execute() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        CostEstimatorDialog dialog = new CostEstimatorDialog(
            null,
            plugin.getHome(),
            plugin.getUserPreferences());
        dialog.setVisible(true);
      }
    });
  }
}
