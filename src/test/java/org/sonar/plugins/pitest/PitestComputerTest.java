/*
 * Sonar Pitest Plugin
 * Copyright (C) 2009-2016 Alexandre Victoor
 * alexvictoor@gmail.com
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

import org.junit.Test;
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerDefinition;
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinitionContext;

public class PitestComputerTest {

  PitestComputer underTest = new PitestComputer();
  
     @Test
     public void test_definition() {
       TestMeasureComputerDefinitionContext defContext = new TestMeasureComputerDefinitionContext();
       MeasureComputerDefinition def = underTest.define(defContext);
       assertThat(def).isNotNull();
       assertThat(def.getOutputMetrics()).containsOnly("my_new_metric");
     }
  
}
