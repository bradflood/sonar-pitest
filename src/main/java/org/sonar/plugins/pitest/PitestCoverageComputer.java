/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2017 Vinod Anandan
 * vinod@owasp.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.pitest;

import org.sonar.api.ce.measure.Measure;
import org.sonar.api.ce.measure.MeasureComputer;

/**
 * Computer for calculating the mutation coverage of a component based on the detected vs total mutations.
 *
 * @author <a href="mailto:gerald.muecke@devcon5.io">Gerald M&uuml;cke</a>
 */
public class PitestCoverageComputer implements MeasureComputer {

  private static final String[] metricKeys = {
    PitestMetrics.MUTATIONS_GENERATED_KEY,
    PitestMetrics.MUTATIONS_KILLED_KEY,
    PitestMetrics.MUTATIONS_KILLED_PERCENT_KEY};

  @Override
  public MeasureComputerDefinition define(final MeasureComputerDefinitionContext defContext) {

    return defContext.newDefinitionBuilder()
      .setOutputMetrics(metricKeys)
      .build();
  }

  @Override
  public void compute(final MeasureComputerContext context) {
    final Measure mutationsTotal = context.getMeasure(PitestMetrics.MUTATIONS_GENERATED_KEY);
    if (mutationsTotal != null) {
      final Integer elements = mutationsTotal.getIntValue();
      final Measure killed = context.getMeasure(PitestMetrics.MUTATIONS_KILLED_KEY);
      if (elements > 0 && killed != null) {
        final Integer coveredElements = killed.getIntValue();
        final Double coverage = 100.0 * coveredElements / elements;
        context.addMeasure(PitestMetrics.MUTATIONS_KILLED_PERCENT_KEY, coverage);
      }
    }
  }
}
