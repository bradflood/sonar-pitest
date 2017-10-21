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
import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class XmlReportFinderTest {

  @Ignore("flawed logic - replace")
  @Test
  public void should_find_report_file() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File xmlFile = new File(Resources.getResource("mutations.xml").getFile());
    File directory = xmlFile.getParentFile();

    // when
    File report = finder.findReport(directory);

    // then
    assertThat(report).isEqualTo(xmlFile);
  }

  @Test
  public void should_return_null_if_no_report() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File directory = new File(Resources.getResource("fake_libs").getFile());

    // when
    File report = finder.findReport(directory);

    // then
    assertThat(report).isNull();
  }

  @Test
  public void should_return_null_if_directory_does_not_exist() {
    // given
    XmlReportFinder finder = new XmlReportFinder();
    File directory = new File("imaginary");

    // when
    File report = finder.findReport(directory);

    // then
    assertThat(report).isNull();
  }
}
