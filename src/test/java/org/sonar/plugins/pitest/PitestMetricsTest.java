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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PitestMetricsTest {
  
  @Test
  public void parse_upper_case_KILLED_succeeds() {
    // given
    
    // when
    MutantStatus status =  MutantStatus.parse("KILLED");
    
    //then
    assertThat(status).isEqualTo(MutantStatus.KILLED);
  }
  
  @Test
  public void parse_lower_case_killed_returns_unknown() {
    // given
    
    // when
    MutantStatus status =  MutantStatus.parse("killed");
    
    //then
    assertThat(status).isEqualTo(MutantStatus.UNKNOWN);
  }
  
  @Test
  public void parse_empty_string_returns_unknown() {
    // given
    
    // when
    MutantStatus status =  MutantStatus.parse("");
    
    //then
    assertThat(status).isEqualTo(MutantStatus.UNKNOWN);
  }
  @Test
  public void parse_null_returns_unknown() {
    // given
    
    // when
    MutantStatus status =  MutantStatus.parse(null);
    
    //then
    assertThat(status).isEqualTo(MutantStatus.UNKNOWN);
  }
}
