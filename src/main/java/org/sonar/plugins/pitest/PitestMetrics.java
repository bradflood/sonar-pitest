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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.sonar.api.measures.Metric;
//import org.sonar.api.batch.measure.Metric
import org.sonar.api.measures.Metrics;

/**
 * Metrics for the sonar pitest plugin.
 * 
 */
public class PitestMetrics implements Metrics {

  public static final String PITEST_DOMAIN = "Mutation analysis";

  public static final String MUTATIONS_NOT_COVERED_KEY = "pitest_mutations_noCoverage";
  public static final String MUTATIONS_GENERATED_KEY = "pitest_mutations_total";
  public static final String MUTATIONS_KILLED_KEY = "pitest_mutations_killed";
  public static final String MUTATIONS_SURVIVED_KEY = "pitest_mutations_survived";
  public static final String MUTATIONS_ERROR_KEY = "pitest_mutations_error";
  public static final String MUTATIONS_UNKNOWN_KEY = "pitest_mutations_unknown";
  public static final String MUTATIONS_DATA_KEY = "pitest_mutations_data"; // needed?
  public static final String MUTATIONS_KILLED_PERCENT_KEY = "pitest_mutations_killed_percent";

  // public static final String MUTATIONS_DETECTED_KEY = "pitest_mutations_detected";
  // public static final String MUTATIONS_NOT_DETECTED_KEY = "pitest_mutations_survived";
  // public static final String MUTATIONS_MEMORY_ERROR_KEY = "pitest_mutations_memoryError";
  // public static final String MUTATIONS_TIMED_OUT_KEY = "pitest_mutations_timedOut";

  // how is this reported from pitest?
  public static final Metric<Serializable> MUTATIONS_NOT_COVERED = new Metric.Builder(MUTATIONS_NOT_COVERED_KEY, "Non Covered Mutations", Metric.ValueType.INT)
    .setDescription("Number of mutations not covered by any test.")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final Metric<Serializable> MUTATIONS_GENERATED = new Metric.Builder(MUTATIONS_GENERATED_KEY, "Total Mutations", Metric.ValueType.INT)
    .setDescription("Total number of mutations generated")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final Metric<Serializable> MUTATIONS_KILLED = new Metric.Builder(MUTATIONS_KILLED_KEY, "Killed Mutations", Metric.ValueType.INT)
    .setDescription("Number of mutations killed by a test.")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final Metric<Serializable> MUTATIONS_SURVIVED = new Metric.Builder(MUTATIONS_SURVIVED_KEY, "Survived Mutations", Metric.ValueType.INT)
    .setDescription("Number of mutations survived")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final Metric<Serializable> MUTATIONS_ERROR = new Metric.Builder(MUTATIONS_ERROR_KEY, "Error Mutations", Metric.ValueType.INT)
    .setDescription("Number of mutations that caused an error")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  public static final Metric<Serializable> MUTATIONS_UNKNOWN = new Metric.Builder(MUTATIONS_UNKNOWN_KEY, "Mutations with unknown status", Metric.ValueType.INT)
    .setDescription("Number of mutations for which status is unknown")
    .setDirection(Metric.DIRECTION_WORST)
    .setQualitative(false)
    .setDomain(PITEST_DOMAIN)
    .create();

  // json representation of the mutation
  public static final Metric<Serializable> MUTATIONS_DATA = new Metric.Builder(MUTATIONS_DATA_KEY, "Mutations Data", Metric.ValueType.DATA)
    .setDescription("Mutations Data")
    .setDirection(Metric.DIRECTION_NONE)
    .setQualitative(true)
    .setDomain(PITEST_DOMAIN)
    .create();

  // should this be renamed to MUTATIONS_DETECTED_RATIO
  public static final Metric<Serializable> MUTATIONS_KILLED_RATIO = new Metric.Builder(MUTATIONS_KILLED_PERCENT_KEY, "Mutations Coverage Ratio", Metric.ValueType.PERCENT)
    .setDescription("Ratio of mutations found by tests")
    .setDirection(Metric.DIRECTION_BETTER)
    .setQualitative(true)
    .setDomain(PITEST_DOMAIN)
    .setBestValue(100d)
    .setWorstValue(0d)
    .create();

  @Override
  public List<Metric> getMetrics() {
    return Arrays.asList(
      MUTATIONS_NOT_COVERED,
      MUTATIONS_GENERATED,
      MUTATIONS_KILLED,
      MUTATIONS_SURVIVED,
      MUTATIONS_ERROR,
      MUTATIONS_UNKNOWN,
      // MUTATIONS_DETECTED,
      // MUTATIONS_NOT_DETECTED,
      MUTATIONS_DATA,
      MUTATIONS_KILLED_RATIO);
  }

  // public static final Metric<Serializable> MUTATIONS_DETECTED = new Metric.Builder(MUTATIONS_DETECTED_KEY, "Detected Mutations",
  // Metric.ValueType.INT)
  // .setDescription("Total number of mutations detected")
  // .setDirection(Metric.DIRECTION_BETTER)
  // .setQualitative(false)
  // .setDomain(PITEST_DOMAIN)
  // .create();

  // public static final Metric<Serializable> MUTATIONS_NOT_DETECTED = new Metric.Builder(MUTATIONS_NOT_DETECTED_KEY, "Undetected
  // Mutations", Metric.ValueType.INT)
  // .setDescription("Number of mutations covered by a test, but not detected by the test")
  // .setDirection(Metric.DIRECTION_WORST)
  // .setQualitative(false)
  // .setDomain(PITEST_DOMAIN)
  // .create();
  //
  // public static final Metric<Serializable> MUTATIONS_MEMORY_ERROR = buildMetric(MUTATIONS_MEMORY_ERROR_KEY, "Memory Error Mutations",
  // "Number of mutations detected by memory errors.",
  // Metric.ValueType.INT, Metric.DIRECTION_BETTER, false, PITEST_DOMAIN);
  //
  // public static final Metric<Serializable> MUTATIONS_TIMED_OUT = buildMetric(MUTATIONS_TIMED_OUT_KEY, "Timed Out Mutations", "Number of
  // mutations detected by time outs.",
  // Metric.ValueType.INT, Metric.DIRECTION_BETTER, false, PITEST_DOMAIN);
  //
  // public static final Metric<Serializable> MUTATIONS_UNKNOWN = buildMetric(MUTATIONS_UNKNOWN_KEY, "Unknown Status Mutations", "Number of
  // mutations with unknown status.",
  // Metric.ValueType.INT, Metric.DIRECTION_WORST, false, PITEST_DOMAIN);

  // private static Metric<Serializable> buildMetric(String key, String name, String description, ValueType valueType, Integer direction,
  // Boolean qualitative, String domain) {
  // return buildMetric(instanceBuilder(key, name, description, valueType, direction, qualitative, domain), qualitative);
  // }
  //
  // private static Metric<Serializable> buildMetric(String key, String name, String description, ValueType valueType, Integer direction,
  // Boolean qualitative, String domain,
  // Double best, Double worst) {
  // Builder builder = instanceBuilder(key, name, description, valueType, direction, qualitative, domain);
  // builder.setBestValue(best);
  // builder.setWorstValue(worst);
  // return buildMetric(builder, qualitative);
  // }
  //
  // private static Metric<Serializable> buildMetric(Builder builder, boolean qualitative) {
  // Metric<Serializable> metric = builder.create();
  // METRICS.add(metric);
  // if (!qualitative) {
  // QUANTITATIVE_METRICS.add(metric);
  // }
  // return metric;
  // }

  // private static Builder instanceBuilder(String key, String name, String description, ValueType valueType, Integer direction, Boolean
  // quailitative, String domain) {
  // Builder builder = new Builder(key, name, valueType);
  // builder.setDescription(description);
  // builder.setDirection(direction);
  // builder.setQualitative(quailitative);
  // builder.setDomain(domain);
  // return builder;
  // }

  /**
   * Returns the pitest quantitative metrics list.
   * @return {@link List<Metric>} The pitest quantitative metrics list.
   */
  // public static List<Metric<Serializable>> getQuantitativeMetrics() {
  // return QUANTITATIVE_METRICS;
  // }
}
