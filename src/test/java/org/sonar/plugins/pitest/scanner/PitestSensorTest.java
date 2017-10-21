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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.plugins.pitest.PitestConstants;
import org.sonar.plugins.pitest.domain.Mutant;
import org.sonar.plugins.pitest.domain.MutantStatus;
import org.sonar.plugins.pitest.scanner.PitestSensor;
import org.sonar.plugins.pitest.scanner.XmlReportFinder;
import org.sonar.plugins.pitest.scanner.XmlReportParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.pitest.PitestConstants.MODE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_REUSE_REPORT;
import static org.sonar.plugins.pitest.PitestConstants.MODE_SKIP;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_DEF;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.REPOSITORY_KEY;

public class PitestSensorTest {

  /*
   * Test note: PitestSensor requires the presence of keys MODE_KEY and REPORT_DIRECTORY_KEY (reference: PitestSensor.describe..
   * onlyWhenConfiguration.
   * If I'm understanding the platform API correctly, the platform will not call the sensor if the keys are not present.
   */

  @Test
  public void should_describe_execution_conditions() throws Exception {
    // given
    SensorContextTester context = createContext();
    Configuration configuration = mockConfiguration();
    PitestSensor sensor = new PitestSensor(configuration, mockXmlReportParserOnJavaFiles(), mockRulesProfile(true, false), mockXmlReportFinder(), context.fileSystem());

    SensorDescriptor descriptor = spy(SensorDescriptor.class);

    // when
    sensor.describe(descriptor);

    // then
    verify(descriptor).name(PitestSensor.SENSOR_NAME);
    verify(descriptor).onlyOnLanguages("java");
    verify(descriptor).onlyOnFileType(InputFile.Type.MAIN);
    verify(descriptor).createIssuesForRuleRepository(REPOSITORY_KEY);
    // verify(descriptor).onlyWhenConfiguration(conf -> configuration.hasKey(MODE_KEY) && configuration.hasKey(REPORT_DIRECTORY_KEY));
  }

  @Test
  public void should_skip_analysis_if_mode_is_skip() throws IOException {
    // given
    Configuration configuration = mock(Configuration.class);
    when(configuration.get(MODE_KEY)).thenReturn(Optional.of(MODE_SKIP));
    SensorContextTester context = createContext();
    PitestSensor sensor = new PitestSensor(configuration, mock(XmlReportParser.class), mock(RulesProfile.class), mock(XmlReportFinder.class), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void should_not_fail_if_no_report_found() throws IOException {
    // given
    Configuration configuration = mock(Configuration.class);
    when(configuration.get(MODE_KEY)).thenReturn(Optional.of(MODE_REUSE_REPORT));
    when(configuration.get(REPORT_DIRECTORY_KEY)).thenReturn(Optional.of("nonexistant-directory"));
    SensorContextTester context = createContext();
    PitestSensor sensor = new PitestSensor(configuration, mock(XmlReportParser.class), mock(RulesProfile.class), mock(XmlReportFinder.class), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void should_create_issue_for_survived_mutant() throws Exception {
    // given
    SensorContextTester context = createContext();
    PitestSensor sensor = new PitestSensor(mockConfiguration(), mockXmlReportParserOnJavaFiles(), mockRulesProfile(true, false), mockXmlReportFinder(), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo(PitestConstants.SURVIVED_MUTANT_RULE_KEY);

  }

  @Test
  public void should_create_issue_for_both_java_and_kotlin_sources() throws Exception {
    // given
    SensorContextTester context = createContext();
    PitestSensor sensor = new PitestSensor(mockConfiguration(), mockXmlReportParserOnMixedFiles(), mockRulesProfile(true, false), mockXmlReportFinder(), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    assertThat(context.allIssues()).hasSize(2);
    for (Issue issue : context.allIssues()) {
      assertThat(issue.ruleKey().rule()).isEqualTo(PitestConstants.SURVIVED_MUTANT_RULE_KEY);
    }

  }

  @Test
  public void should_not_create_issue_for_survived_mutant_if_present_but_rule_not_active() throws Exception {
    // given
    SensorContextTester context = createContext();
    PitestSensor sensor = new PitestSensor(mockConfiguration(), mockXmlReportParserOnJavaFiles(), mockRulesProfile(false, false), mockXmlReportFinder(), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    assertThat(context.allIssues()).isEmpty();

  }
  
//  // FIXME: investigate API requirement here
//  @Test
//  public void should_create_measure_if_no_rules_active() throws Exception {
//    // given
//    SensorContextTester context = createContext();
//    PitestSensor sensor = new PitestSensor(mockConfiguration(), mockXmlReportParserOnJavaFiles(), mockRulesProfile(false, false), mockXmlReportFinder(), context.fileSystem());
//
//    // when
//    sensor.execute(context);
//
//    // then
//    //assertThat(context.measure(componentKey, metric)
//
//  }

  @Test
  public void should_create_issue_for_coverage_not_met() throws Exception {
    // given
    SensorContextTester context = createContext();
    PitestSensor sensor = new PitestSensor(mockConfiguration(), mockXmlReportParserOnJavaFiles(), mockRulesProfile(false, true), mockXmlReportFinder(), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    assertThat(context.allIssues()).hasSize(1);
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo(PitestConstants.INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY);

  }

  @Test
  public void should_not_create_issue_for_coverage_not_met_if_coverage_below_threshold() throws Exception {
    // given
    SensorContextTester context = createContext();
    RulesProfile mockRulesProfile = mockRulesProfile(false, true);
    ActiveRule mockCoverageRule = mockRulesProfile.getActiveRule(PitestConstants.REPOSITORY_KEY, PitestConstants.INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY);
    when(mockCoverageRule.getParameter(PitestConstants.COVERAGE_RATIO_PARAM)).thenReturn("10");
    PitestSensor sensor = new PitestSensor(mockConfiguration(), mockXmlReportParserOnJavaFiles(), mockRulesProfile, mockXmlReportFinder(), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    assertThat(context.allIssues()).isEmpty();
  }

  @Test
  public void should_not_create_issue_for_coverage_not_met_if_rule_not_active() throws Exception {
    // given
    SensorContextTester context = createContext();
    PitestSensor sensor = new PitestSensor(mockConfiguration(), mockXmlReportParserOnJavaFiles(), mockRulesProfile(false, false), mockXmlReportFinder(), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    assertThat(context.allIssues()).isEmpty();

  }

  @Test
  public void verifyMeasures() throws Exception {
    // given
    SensorContextTester context = createContext();
    PitestSensor sensor = new PitestSensor(mockConfiguration(), mockXmlReportParserOnJavaFiles(), mockRulesProfile(false, false), mockXmlReportFinder(), context.fileSystem());

    // when
    sensor.execute(context);

    // then
    //context.newCoverage()
    assertThat(context.allIssues()).isEmpty();

  }
 
  // context.measures()
  // context.
  /*
   * public <G extends Serializable> Measure<G> measure(String componentKey, Metric<G> metric) {
   * return measure(componentKey, metric.key());
   * }
   * 
   * public <G extends Serializable> Measure<G> measure(String componentKey, String metricKey) {
   * return sensorStorage.measuresByComponentAndMetric.row(componentKey).get(metricKey);
   * }
   * 
   * context.allAnalysisErrors
   * 
   * context.linehits
   */
  /*
   * 
   */

  private Configuration mockConfiguration() {
    Configuration configuration = mock(Configuration.class);
    when(configuration.get(MODE_KEY)).thenReturn(Optional.of(MODE_REUSE_REPORT));
    when(configuration.get(REPORT_DIRECTORY_KEY)).thenReturn(Optional.of(REPORT_DIRECTORY_DEF));
    return configuration;
  }

  private XmlReportParser mockXmlReportParserOnJavaFiles() {
    XmlReportParser xmlReportParser = mock(XmlReportParser.class);
    List<Mutant> mutantsOnJavaFiles = mutantsOnJavaFiles();
    when(xmlReportParser.parse(any(File.class))).thenReturn(mutantsOnJavaFiles);
    return xmlReportParser;
  }

  private XmlReportParser mockXmlReportParserOnMixedFiles() {
    XmlReportParser xmlReportParser = mock(XmlReportParser.class);
    List<Mutant> mutantsOnMixedFiles = mutantsOnMixedFiles();
    when(xmlReportParser.parse(any(File.class))).thenReturn(mutantsOnMixedFiles);
    return xmlReportParser;
  }

  private XmlReportFinder mockXmlReportFinder() {
    XmlReportFinder xmlReportFinder = mock(XmlReportFinder.class);
    when(xmlReportFinder.findReport(any(File.class))).thenReturn(new File("fake-report.xml"));
    return xmlReportFinder;
  }

  private RulesProfile mockRulesProfile(boolean survivedMutantRuleActive, boolean coverageRuleActive) {
    RulesProfile qualityProfile = mock(RulesProfile.class);
    when(qualityProfile.getName()).thenReturn("fake pit profile");

    if (survivedMutantRuleActive) {
      ActiveRule survivedMutantRule = mock(ActiveRule.class);
      when(survivedMutantRule.getRule()).thenReturn(Rule.create());
      when(qualityProfile.getActiveRule(PitestConstants.REPOSITORY_KEY, PitestConstants.SURVIVED_MUTANT_RULE_KEY)).thenReturn(survivedMutantRule);
    }
    if (coverageRuleActive) {
      ActiveRule coverageRule = mock(ActiveRule.class);
      when(coverageRule.getParameter(PitestConstants.COVERAGE_RATIO_PARAM)).thenReturn("50");
      when(coverageRule.getRule()).thenReturn(Rule.create());
      when(qualityProfile.getActiveRule(PitestConstants.REPOSITORY_KEY, PitestConstants.INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY)).thenReturn(coverageRule);

    }
    return qualityProfile;
  }

  private SensorContextTester createContext() throws IOException {
    SensorContextTester context = SensorContextTester.create(new File("src/test/resources/"));
    DefaultFileSystem fs = context.fileSystem();

    createTestJavaFile(fs);
    createTestKotlinFile(fs);
    return context;
  }

  private void createTestJavaFile(DefaultFileSystem fs) throws IOException {
    String effectiveKey = "com/foo/Bar.java";
    File file = new File(fs.baseDir(), effectiveKey);
    DefaultInputFile inputFile = new TestInputFileBuilder("module.key", effectiveKey).setLanguage("java").setModuleBaseDir(fs.baseDirPath())
      .setType(InputFile.Type.MAIN)
      .setLines(1000)
      .setOriginalLineOffsets(new int[] {0, 2, 10, 42, 1000})
      .setLastValidOffset(1)
      .initMetadata(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8))
      .setCharset(StandardCharsets.UTF_8)
      .build();
    fs.add(inputFile);
  }

  private void createTestKotlinFile(DefaultFileSystem fs) throws IOException {
    String effectiveKey = "Maze.kt";
    File file = new File(fs.baseDir(), effectiveKey);
    // FIXME: unclear the context for setting the language; it was nullable, so I removed it for now
    DefaultInputFile inputFile = new TestInputFileBuilder("module.key", effectiveKey).setModuleBaseDir(fs.baseDirPath())
      .setType(InputFile.Type.MAIN)
      .setLines(1000)
      .setOriginalLineOffsets(new int[] {0, 2, 10, 42, 1000})
      .setLastValidOffset(1)
      .initMetadata(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8))
      .setCharset(StandardCharsets.UTF_8)
      .build();
    fs.add(inputFile);
  }

  private List<Mutant> mutantsOnJavaFiles() {
    List<Mutant> mutants = new ArrayList<>();
    mutants.add(new Mutant(true, MutantStatus.KILLED, "com.foo.Bar", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.SURVIVED, "com.foo.Bar", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.NO_COVERAGE, "com.foo.Bar", 2, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.UNKNOWN, "com.foo.Bar", 0, null));
    return mutants;
  }

  private List<Mutant> mutantsOnMixedFiles() {
    List<Mutant> mutants = new ArrayList<>();
    mutants.add(new Mutant(true, MutantStatus.KILLED, "com.foo.Bar", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.SURVIVED, "com.foo.Bar", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.SURVIVED, "some.Maze", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "Maze.kt"));
    mutants.add(new Mutant(false, MutantStatus.NO_COVERAGE, "com.foo.Bar", 2, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator"));
    mutants.add(new Mutant(false, MutantStatus.UNKNOWN, "com.foo.Bar", 0, null));
    return mutants;
  }

}
