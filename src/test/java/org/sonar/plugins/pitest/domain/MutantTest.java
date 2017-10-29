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
package org.sonar.plugins.pitest.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MutantTest {

  public static final String INLINE_CONSTANT_MUTATOR = "org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator";
  public static final String RETURN_VALS_MUTATOR = "org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator";
  public static final String CONSTRUCTOR_MUTATOR = Mutator.CONSTRUCTOR.getKey();

  @Test
  public void should_get_path_to_java_source_file() {
    // given
    Mutant mutant = new TestMutantBuilder().className("com.foo.Bar").sourceFile("Bar.java").build();
    // when
    String path = mutant.sourceRelativePath();
    // then
    assertThat(path).isEqualTo("com/foo/Bar.java");
  }



//  @Test
//  public void should_get_path_to_source_file_for_an_anonymous_inner_class() {
//    // given
//    Mutant mutant = new TestMutantBuilder().className(mutator(Mutator.CONSTRUCTOR).build();
//    //new Mutant(true, MutantStatus.KILLED, "com.foo.Bar$1", "mutatedMethod", 17, INLINE_CONSTANT_MUTATOR, "com/foo/Bar.java");
//    // when
//    String path = mutant.sourceRelativePath();
//    // then
//    assertThat(path).isEqualTo("com/foo/Bar.java");
//  }

  @Test
  public void verify_description() {
    // given
    Mutant mutant = new TestMutantBuilder().mutator(Mutator.CONSTRUCTOR).build();
    // when
    String path = mutant.violationDescription();
    // then
    assertThat(path).isEqualTo("A constructor call has been removed without breaking the tests");
  }

  @Test
  public void verify_json() {
    // given
    Mutant mutant = new TestMutantBuilder().mutantStatus(MutantStatus.SURVIVED).className("com.foo.Bar").mutatedMethod("mutatedMethod").lineNumber(17).mutator(Mutator.CONSTRUCTOR).sourceFile("Bar.kt").build(); 
    // when
    String string = mutant.toString();
    // then
    assertThat(string).isEqualTo(
      "{ \"d\" : true, \"s\" : \"SURVIVED\", \"c\" : \"com.foo.Bar\", \"mname\" : \"Constructor Calls Mutator\", \"mdesc\" : \"A constructor call has been removed\", \"sourceFile\" : \"Bar.kt\", \"mmethod\" : \"mutatedMethod\", \"l\" : \"17\" }");
  }

  @Test
  public void verify_json_with_killing_test() {
    // given
    Mutant mutant = new TestMutantBuilder().mutantStatus(MutantStatus.KILLED).className("com.foo.Bar").mutatedMethod("mutatedMethod").lineNumber(17).mutator(Mutator.CONSTRUCTOR).sourceFile("Bar.kt").killingTest("killingTest").build(); 
    // when
    String string = mutant.toString();
    // then
    assertThat(string).isEqualTo(
      "{ \"d\" : true, \"s\" : \"KILLED\", \"c\" : \"com.foo.Bar\", \"mname\" : \"Constructor Calls Mutator\", \"mdesc\" : \"A constructor call has been removed\", \"sourceFile\" : \"Bar.kt\", \"mmethod\" : \"mutatedMethod\", \"l\" : \"17\", \"killtest\" : \"killingTest\" }");
  }
  
}
