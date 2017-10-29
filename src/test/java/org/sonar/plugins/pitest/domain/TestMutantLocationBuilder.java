package org.sonar.plugins.pitest.domain;

import java.util.concurrent.ThreadLocalRandom;

public class TestMutantLocationBuilder {
  private String className = random("className");
  private String sourceFile = randomSourceFile("sourceFile");
  private String mutatedMethod = random("mutatedMethod");
  private String methodDescription = random("methodDescription");
  
  private int lineNumber = ThreadLocalRandom.current().nextInt(1, 101);
  
  public TestMutantLocationBuilder() {
    
  }
 
  public TestMutantLocationBuilder className(String className) {
    this.className = className;
    return this;
  }
  
  public TestMutantLocationBuilder sourceFile(String sourceFile) {
    this.sourceFile = sourceFile;
    return this;
  }
  public TestMutantLocationBuilder mutatedMethod(String mutatedMethod) {
    this.mutatedMethod = mutatedMethod;
    return this;
  }
  public TestMutantLocationBuilder methodDescription(String methodDescription) {
    this.methodDescription = methodDescription;
    return this;
  }
  public TestMutantLocationBuilder lineNumber(int lineNumber) {
    this.lineNumber = lineNumber;
    return this;
  }
  
  public MutantLocation build() {
    return new MutantLocation(this.className, this.sourceFile, this.mutatedMethod, this.methodDescription, this.lineNumber);
  }
  
  private String random(String in) {
    return in + ThreadLocalRandom.current().nextInt(0, 101);
  }
  
  private String randomSourceFile(String in) {
    StringBuilder builder = new StringBuilder(random(in));
    return builder.append(randomExtension()).toString();
  }
  
  private String randomExtension() {
    return ThreadLocalRandom.current().nextInt(0, 1) == 0 ? ".java" : ".kt" ;
  }
}
