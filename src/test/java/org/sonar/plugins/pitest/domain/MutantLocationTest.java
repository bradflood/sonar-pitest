package org.sonar.plugins.pitest.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MutantLocationTest {

  @Test
  public void should_calculate_relative_path_java() {
    // given
    MutantLocation mutantLocation = new TestMutantLocationBuilder().sourceFile("Bar.java").className("com.foo.Bar").build();
    
    // when
    String path = mutantLocation.getRelativePath();
    
    // then
    assertThat(path).isEqualTo("com/foo/Bar.java");
  }
  
  @Test
  public void should_calculate_relative_path_java_inner_class() {
    // given
    MutantLocation mutantLocation = new TestMutantLocationBuilder().sourceFile("Bar.java").className("com.foo.Bar$1").build();
    
    // when
    String path = mutantLocation.getRelativePath();
    
    // then
    assertThat(path).isEqualTo("com/foo/Bar.java");
  }
  
  @Test
  public void should_calculate_relative_path_kotlin() {
    // given
    MutantLocation mutantLocation = new TestMutantLocationBuilder().sourceFile("MainKotlin.kt").className("some.Hello").build();
    
    // when
    String path = mutantLocation.getRelativePath();
    
    // then
    assertThat(path).isEqualTo("MainKotlin.kt");
  }  
}
