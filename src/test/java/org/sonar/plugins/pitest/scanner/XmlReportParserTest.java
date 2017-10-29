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
package org.sonar.plugins.pitest.scanner;

import com.google.common.io.Resources;
import java.io.File;
import java.util.Collection;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.sonar.plugins.pitest.domain.Mutant;
import org.sonar.plugins.pitest.domain.MutantStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlReportParserTest {
  private static final String MODULE_BASE_DIR = "src/test/resources/xml-report-parser-test" ;

  private XmlReportParser parser;

  @Before
  public void setUp() {
    parser = new XmlReportParser();
  }

  @Ignore("WIP")
  @Test
  public void should_parse_report_and_find_mutants() {
    // given
    File report = new File(MODULE_BASE_DIR, "mutations.xml");

    // when
    Collection<Mutant> mutants = parser.parse(report);

    // then
    assertThat(mutants).hasSize(46);

//    assertThat(mutants).usingFieldByFieldElementComparator().contains(new Mutant(true, MutantStatus.KILLED, "org.sonar.plugins.csharp.gallio.GallioSensor", "mutatedMethod", 87,
//      "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "GallioSensor.java"));
//    assertThat(mutants).usingFieldByFieldElementComparator().contains(new Mutant(false, MutantStatus.NO_COVERAGE, "org.sonar.plugins.csharp.gallio.GallioSensor", "mutatedMethod", 162,
//      "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator", "GallioSensor.java"));
//    assertThat(mutants).usingFieldByFieldElementComparator().contains(new Mutant(false, MutantStatus.SURVIVED, "org.sonar.plugins.csharp.gallio.GallioSensor", "mutatedMethod", 166,
//      "org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator", "GallioSensor.java"));
//    assertThat(mutants).extracting("lineNumber").contains(166);
//    assertThat(mutants).extracting("mutantStatus").doesNotContain(MutantStatus.UNKNOWN);
  }

  @Ignore("WIP")
  @Test
  public void should_parse_report_and_find_mutants_if_elements_are_unordered() {
    // given
    File report = new File(Resources.getResource("mutations-elements-out-of-order.xml").getFile());

    // when
    Collection<Mutant> mutants = parser.parse(report);

    // then
    assertThat(mutants).hasSize(1);

//    assertThat(mutants).usingFieldByFieldElementComparator().contains(new Mutant(true, MutantStatus.KILLED, "org.sonar.plugins.csharp.gallio.GallioSensor", "mutatedMethod", 85,
//      "org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator", "GallioSensor.java"));
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void should_throw_exception_if_file_is_missing() {
    // given

    // when
    new File(Resources.getResource("imaginary").getFile());

    // then
  }

  @Test(expected = IllegalStateException.class)
  public void should_throw_exception_if_file_is_invalid() {
    // given

    // when
    File report = new File(Resources.getResource("mutations-invalid-format.xml").getFile());
    parser.parse(report);

    // then
  }

  @Test
  public void should_log_but_not_throw_exception_if_line_number_parsing_fails() {
    // given

    // when
    File report = new File(Resources.getResource("mutations-invalid-format-line-number.xml").getFile());
    parser.parse(report);

    // then
  }
}
