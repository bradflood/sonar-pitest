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

import java.io.File;
import java.io.IOException;
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
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.config.Configuration;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sonar.plugins.pitest.PitestConstants.MODE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_REUSE_REPORT;
import static org.sonar.plugins.pitest.PitestConstants.MODE_SKIP;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_DEF;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_KEY;

public class PitestSensorTest {

  private PitestSensor sensor;


  @Test
  public void should_skip_analysis_if_mode_is_skip() throws IOException {
    // given
    Configuration configuration = mock(Configuration.class);
    when(configuration.get(MODE_KEY)).thenReturn(Optional.of(MODE_SKIP));
    SensorContextTester context = createContext();
    sensor = new PitestSensor(configuration, mock(XmlReportParser.class), mock(RulesProfile.class), mock(XmlReportFinder.class), context.fileSystem());
    

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
    SensorContextTester context = createContext();
    sensor = new PitestSensor(configuration, mock(XmlReportParser.class), mock(RulesProfile.class), mock(XmlReportFinder.class), context.fileSystem());

    // when
    sensor.execute(context);
    
    // then
    assertThat(context.allIssues()).isEmpty();
  }
  
  @Test
  public void should_create_issue_for_survived_mutant_if_present() throws Exception {
    // given
    SensorContextTester context = createContext();    
    sensor = new PitestSensor(standardMockConfiguration(), standardMockXmlReportParser(), mockRulesProfile(true, false), standardMockXmlReportFinder(), context.fileSystem());


    // when
    sensor.execute(context);
    
    // then
    assertThat(context.allIssues()).isNotEmpty();
    Issue issue = context.allIssues().iterator().next();
    assertThat(issue.ruleKey().rule()).isEqualTo(PitestConstants.SURVIVED_MUTANT_RULE_KEY);
    
  }  

  
  
  
  
  
  private Configuration standardMockConfiguration() {
    Configuration configuration = mock(Configuration.class);
    when(configuration.get(MODE_KEY)).thenReturn(Optional.of(MODE_REUSE_REPORT));
    when(configuration.get(REPORT_DIRECTORY_KEY)).thenReturn(Optional.of(REPORT_DIRECTORY_DEF));
    return configuration;
  }

  
  private XmlReportParser standardMockXmlReportParser() {
    XmlReportParser xmlReportParser = mock(XmlReportParser.class) ;
    List<Mutant> mutantsOnJavaFiles = createMutantsOnJavaFiles();
    when(xmlReportParser.parse(any(File.class))).thenReturn(mutantsOnJavaFiles);
    return xmlReportParser ; 
  }
  
  private XmlReportFinder standardMockXmlReportFinder() {
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
      when(coverageRule.getRule()).thenReturn(Rule.create());
      when(qualityProfile.getActiveRule(PitestConstants.REPOSITORY_KEY, PitestConstants.SURVIVED_MUTANT_RULE_KEY)).thenReturn(coverageRule);
    }
    return qualityProfile ;
  }
  
 

  
  private SensorContextTester createContext() throws IOException {
    SensorContextTester context = SensorContextTester.create(new File("src/test/resources/"));
    DefaultFileSystem fs = context.fileSystem();

    String effectiveKey = "com/foo/Bar.java";
    File file = new File(fs.baseDir(), effectiveKey);
    DefaultInputFile inputFile = new TestInputFileBuilder("module.key", effectiveKey).setLanguage("java").setModuleBaseDir(fs.baseDirPath())
      .setType(InputFile.Type.MAIN)
      .setLines(1000)
      .setOriginalLineOffsets(new int[]{0, 2, 10, 42, 1000})
      .setLastValidOffset(1)
      .initMetadata(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8))
      .setCharset(StandardCharsets.UTF_8)
      .build();
    fs.add(inputFile);
    return context;
  }
  
  
  private List<Mutant> createMutantsOnJavaFiles() {
    List<Mutant> mutants = new ArrayList<>();
    mutants.add(new Mutant(true, MutantStatus.KILLED, "com.foo.Bar", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "com/foo/Bar.java"));
    mutants.add(new Mutant(false, MutantStatus.SURVIVED, "com.foo.Bar", 10, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "com/foo/Bar.java"));
    mutants.add(new Mutant(false, MutantStatus.NO_COVERAGE, "com.foo.Bar", 2, "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator", "com/foo/Bar.java"));
    mutants.add(new Mutant(false, MutantStatus.MEMORY_ERROR, "com.foo.Bar", 1000, null, "com/foo/Bar.java"));
    mutants.add(new Mutant(false, MutantStatus.UNKNOWN, "com.foo.Bar", 0, null, "com/foo/Bar.java"));
    return mutants;
  }
}
