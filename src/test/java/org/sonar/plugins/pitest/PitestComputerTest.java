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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.sonar.api.ce.measure.Component;
import org.sonar.api.ce.measure.Settings;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerDefinition;
import org.sonar.api.ce.measure.test.TestIssue;
import org.sonar.api.ce.measure.test.TestMeasureComputerContext;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinitionContext;

public class PitestComputerTest {

  @Test
  public void definition_should_have_no_input_metrics_eight_output_metrics() {
    // given
    PitestComputer sut = new PitestComputer();
    TestMeasureComputerDefinitionContext defContext = new TestMeasureComputerDefinitionContext();

    // when
    MeasureComputerDefinition def = sut.define(defContext);

    // then
    assertThat(def).isNotNull();
    assertThat(def.getInputMetrics()).isEmpty();
    assertThat(def.getOutputMetrics()).containsOnly("pitest_mutations_detected", "pitest_mutations_timedOut", "pitest_mutations_noCoverage", "pitest_mutations_killed",
      "pitest_mutations_survived", "pitest_mutations_unknown", "pitest_mutations_total", "pitest_mutations_memoryError");

  }
  
  @Test
  public void compute() {
    // given
    PitestComputer sut = new PitestComputer();
    MeasureComputerDefinition measureComputerDefinition = sut.define(new TestMeasureComputerDefinitionContext());
    //public TestMeasureComputerContext(Component component, Settings settings, MeasureComputerDefinition definition) {
    TestMeasureComputerContext context = new TestMeasureComputerContext(null, null, measureComputerDefinition);
    context.addMeasure("pitest_mutations_detected", 3);
    
    // when
    sut.compute(context);
    //context.setIssues(Arrays.asList(new TestIssue.Builder().setKey("ABCD").build()));

    // then
    assertThat(context.getMeasure("my_new_metric")).isEqualTo(0.5);
    

  }
 
  
}
