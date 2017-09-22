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

import static org.sonar.plugins.pitest.PitestConstants.COVERAGE_RATIO_PARAM;
import static org.sonar.plugins.pitest.PitestConstants.INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_KEY;
import static org.sonar.plugins.pitest.PitestConstants.MODE_SKIP;
import static org.sonar.plugins.pitest.PitestConstants.REPORT_DIRECTORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.REPOSITORY_KEY;
import static org.sonar.plugins.pitest.PitestConstants.SURVIVED_MUTANT_RULE_KEY;

import java.io.Serializable;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rules.ActiveRule;

/**
 * Sonar sensor for pitest mutation coverage analysis.
 *
 * <a href="mailto:aquiporras@gmail.com">Jaime Porras L&oacute;pez</a>
 * <a href="mailto:vinod@owasp.org">Alexandre Victoor</a>
 */
public class PitestSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(PitestSensor.class);

  private final Configuration configuration;
  private final XmlReportParser parser;
  private final XmlReportFinder xmlReportFinder;
  private final String executionMode;
  private final RulesProfile rulesProfile;
  private final FileSystem fileSystem;
  private final FilePredicate mainFilePredicate;

  public PitestSensor(Configuration configuration, XmlReportParser parser, RulesProfile rulesProfile, XmlReportFinder xmlReportFinder, FileSystem fileSystem) {
    this.configuration = configuration;
    this.parser = parser;
    this.xmlReportFinder = xmlReportFinder;
    this.fileSystem = fileSystem;
    this.executionMode = configuration.get(MODE_KEY).orElse(null);
    this.rulesProfile = rulesProfile;

    this.mainFilePredicate = fileSystem.predicates().and(
      fileSystem.predicates().hasType(InputFile.Type.MAIN),
      fileSystem.predicates().hasLanguage("java"));
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.onlyOnLanguage("java");
    descriptor.name("Pitest Sensor");
  }

  @Override
  public void execute(SensorContext context) {
    if (!fileSystem.hasFiles(mainFilePredicate) || MODE_SKIP.equals(executionMode)) {
      return;
    }

    java.io.File projectDirectory = fileSystem.baseDir();
    String reportDirectoryPath = configuration.get(REPORT_DIRECTORY_KEY).orElse(null);

    java.io.File reportDirectory = new java.io.File(projectDirectory, reportDirectoryPath);
    java.io.File xmlReport = xmlReportFinder.findReport(reportDirectory);
    if (xmlReport == null) {
      LOG.warn("No XML PIT report found in directory {} !", reportDirectory);
      LOG.warn("Checkout plugin documentation for more detailed explanations: https://github.com/SonarQubeCommunity/sonar-pitest");
    } else {
      Collection<Mutant> mutants = parser.parse(xmlReport);
      ProjectReport projectReport = ProjectReport.buildFromMutants(mutants);
      processProjectReport(projectReport, context);
    }


  }


  private void processProjectReport(ProjectReport projectReport, SensorContext context) {
    Collection<SourceFileReport> sourceFileReports = projectReport.getSourceFileReports();
    ActiveRule mutantRule = rulesProfile.getActiveRule(REPOSITORY_KEY, SURVIVED_MUTANT_RULE_KEY);
    ActiveRule coverageRule = rulesProfile.getActiveRule(REPOSITORY_KEY, INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY);

    for (SourceFileReport sourceFileReport : sourceFileReports) {
      InputFile inputFile = locateFile(sourceFileReport.sourceFileRelativePath);
      if (inputFile == null) {
        LOG.warn("Mutation in an unknown resource: {}", sourceFileReport.sourceFileRelativePath);
        if (LOG.isDebugEnabled()) {
          LOG.debug("File report: {}", sourceFileReport.toJSON());
        }
      }
      else {
        generateViolations(context, inputFile, sourceFileReport, mutantRule, coverageRule);
        saveFileMeasures(context, inputFile, sourceFileReport);
      }
    }
  }

  private void generateViolations(SensorContext context, InputFile inputFile, SourceFileReport sourceFileReport, ActiveRule mutantRule, ActiveRule coverageRule) {

    if (mutantRule != null) {
      generateMutantViolations(context, inputFile, sourceFileReport);
    }
    if (coverageRule != null) {
      generateCoverageViolations(context, inputFile, sourceFileReport, coverageRule);
    }

  }

  private void saveFileMeasures(SensorContext context, InputFile inputFile, SourceFileReport sourceFileReport) {
    int  detected = sourceFileReport.getMutationsDetected();
    int total = sourceFileReport.getMutationsTotal();

    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_TOTAL, total);
    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_NO_COVERAGE, sourceFileReport.getMutationsNoCoverage());
    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_KILLED, sourceFileReport.getMutationsKilled());
    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_SURVIVED, sourceFileReport.getMutationsSurvived());
    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_MEMORY_ERROR, sourceFileReport.getMutationsMemoryError());
    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_TIMED_OUT, sourceFileReport.getMutationsTimedOut());
    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_UNKNOWN, sourceFileReport.getMutationsUnknown());
    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_DETECTED, detected);


    String json = sourceFileReport.toJSON();
    saveMetricOnFile(context, inputFile, PitestMetrics.MUTATIONS_DATA, json);

  }

  private <T extends Serializable> void saveMetricOnFile(SensorContext context, InputFile inputFile, Metric<T> metric, T value) {
    context.<T>newMeasure()
      .withValue(value)
      .forMetric(metric)
      .on(inputFile)
      .save();
  }

  private void generateCoverageViolations(SensorContext context, InputFile inputFile, SourceFileReport sourceFileReport, ActiveRule coverageRule) {
    int detected = sourceFileReport.getMutationsDetected();
    int total = sourceFileReport.getMutationsTotal();
    int threshold = Integer.parseInt(coverageRule.getParameter(COVERAGE_RATIO_PARAM));
    if (detected * 100d / total < threshold) {
      int missingMutants = Math.max(1, total * threshold / 100 - detected);
      String issueMsg
        = missingMutants + " more mutants need to be covered by unit tests to reach the minimum threshold of "
        + threshold + "% mutant coverage";

      NewIssue newIssue = context.newIssue();

      NewIssueLocation location = newIssue.newLocation()
        .on(inputFile)
        .message(issueMsg);

      newIssue.at(location);

      newIssue.forRule(RuleKey.of(REPOSITORY_KEY, INSUFFICIENT_MUTATION_COVERAGE_RULE_KEY));
      newIssue.save();
    }
  }

  private void generateMutantViolations(SensorContext context, InputFile inputFile, SourceFileReport sourceFileReport) {
    Collection<Mutant> mutants = sourceFileReport.getMutants();
    for (Mutant mutant : mutants) {
      if (MutantStatus.SURVIVED.equals(mutant.mutantStatus)) {

        NewIssue newIssue = context.newIssue();

        NewIssueLocation location = newIssue.newLocation()
          .on(inputFile)
          .at(inputFile.selectLine(mutant.lineNumber))
          .message(mutant.violationDescription());

        newIssue.at(location);
        newIssue.forRule(RuleKey.of(REPOSITORY_KEY, SURVIVED_MUTANT_RULE_KEY));
        newIssue.save();
      }
    }
  }

  private InputFile locateFile(String sourceFileRelativePath) {
    FilePredicate filePredicate =
      fileSystem.predicates().and(
        fileSystem.predicates().hasType(InputFile.Type.MAIN),
        fileSystem.predicates().matchesPathPattern("**/" + sourceFileRelativePath)
      );
    return fileSystem.inputFile(filePredicate);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
