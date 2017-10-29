package org.sonar.plugins.pitest.domain;

import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

public class TestMutantBuilder {
  private static final int mutatorKeyLength = Mutator.values().length;

  private boolean detected = true;
  private MutantStatus mutantStatus = MutantStatus.values()[ThreadLocalRandom.current().nextInt(0, 4)];
  private TestMutantLocationBuilder mutantLocationBuilder = new TestMutantLocationBuilder();
  private Mutator mutator = Mutator.values()[ThreadLocalRandom.current().nextInt(0, mutatorKeyLength)];
  private int index = ThreadLocalRandom.current().nextInt(0, 10);
  private String killingTest = null;
  private String description = random("description");

  public TestMutantBuilder detected(boolean detected) {
    this.detected = detected;
    return this;
  }

  public TestMutantBuilder mutantStatus(MutantStatus mutantStatus) {
    this.mutantStatus = mutantStatus;
    return this;
  }

  public TestMutantBuilder className(String className) {
    this.mutantLocationBuilder.className(className);
    return this;
  }

  public TestMutantBuilder sourceFile(String sourceFile) {
    this.mutantLocationBuilder.sourceFile(sourceFile);
    return this;
  }

  public TestMutantBuilder mutatedMethod(String mutatedMethod) {
    this.mutantLocationBuilder.mutatedMethod(mutatedMethod);
    return this;
  }

  public TestMutantBuilder lineNumber(int lineNumber) {
    this.mutantLocationBuilder.lineNumber(lineNumber);
    return this;
  }

  public TestMutantBuilder mutator(Mutator mutator) {
    this.mutator = mutator;
    return this;
  }

  public TestMutantBuilder index(int index) {
    this.index = index;
    return this;
  }

  public TestMutantBuilder killingTest(String killingTest) {
    this.killingTest = killingTest;
    return this;
  }

  public TestMutantBuilder description(String description) {
    this.description = description;
    return this;
  }

  public Mutant build() {
    if (mutantStatus.equals(MutantStatus.KILLED) && killingTest == null) {
      killingTest = random("killingtest");
    }
    return new Mutant(detected, mutantStatus, mutantLocationBuilder.build(), mutator.getKey(), index, description, killingTest);
  }

  private static String random(String in) {
    return in + ThreadLocalRandom.current().nextInt(0, 101);
  }
}
