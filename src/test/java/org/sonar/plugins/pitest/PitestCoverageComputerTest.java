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

import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerDefinition;
import org.sonar.api.ce.measure.Settings;
import org.sonar.api.ce.measure.test.TestIssue;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinitionContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class PitestCoverageComputerTest {

  @Test
  public void test_definition() {
    TestMeasureComputerDefinitionContext defContext = new TestMeasureComputerDefinitionContext();
    PitestCoverageComputer coverageComputer = new PitestCoverageComputer();

    MeasureComputerDefinition def = coverageComputer.define(defContext);
    assertThat(def).isNotNull();
    assertThat(def.getInputMetrics()).containsOnly(PitestMetrics.MUTATIONS_GENERATED_KEY, PitestMetrics.MUTATIONS_DETECTED_KEY);
    assertThat(def.getOutputMetrics()).containsOnly(PitestMetrics.MUTATIONS_COVERED_RATIO_KEY);

  }

  @Ignore("WIP")
  @Test
  public void calculateCoverageRatio() {
    // given
    PitestCoverageComputer coverageComputer = new PitestCoverageComputer();
    TestMeasureComputerDefinitionContext defContext = new TestMeasureComputerDefinitionContext();
    TestMeasureComputerContext context = new TestMeasureComputerContext(mock(Component.class), mock(Settings.class), coverageComputer.define(defContext));
    context.addMeasure(PitestMetrics.MUTATIONS_GENERATED_KEY, 10);
    context.addMeasure(PitestMetrics.MUTATIONS_DETECTED_KEY, 2);

    context.setIssues(Arrays.asList(new TestIssue.Builder().setKey("ABCD").build()));

    // when
    coverageComputer.compute(context);

    // then
    assertThat(context.getMeasure(PitestMetrics.MUTATIONS_COVERED_RATIO_KEY)).isEqualTo(0.2);    

  }



}
