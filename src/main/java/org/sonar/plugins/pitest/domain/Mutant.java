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

import java.util.StringTokenizer;

/**
 * Mutation information from the pitest report.
 *
 * @author Jaime Porras
 */
public class Mutant {

  public final boolean detected;
  public final MutantStatus mutantStatus;
  public final String className;
  public final String sourceFile;
  public final int lineNumber;
  public final Mutator mutator;

  public Mutant(boolean detected, MutantStatus mutantStatus, String className, int lineNumber, String mutatorKey, String sourceFile) {
    this.detected = detected;
    this.mutantStatus = mutantStatus;
    this.className = className;
    this.sourceFile = sourceFile;
    this.lineNumber = lineNumber;
    this.mutator = Mutator.parse(mutatorKey);
  }

  public Mutant(boolean detected, MutantStatus mutantStatus, String className, int lineNumber, String mutatorKey) {
    this.detected = detected;
    this.mutantStatus = mutantStatus;
    this.className = className;

    final StringTokenizer tok = new StringTokenizer(className, "$");
    final String classNameFiltered = tok.nextToken();
    this.sourceFile = classNameFiltered.replace('.', '/') + ".java";
    this.lineNumber = lineNumber;
    this.mutator = Mutator.parse(mutatorKey);
  }

  public String sourceRelativePath() {
      return sourceFile;

  }

   public String violationDescription() {
    return mutator.getDescription() + " without breaking the tests";
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("{ \"d\" : ");
    builder.append(detected).append(", \"s\" : \"").append(mutantStatus).append("\", \"c\" : \"").append(className).append("\", \"mname\" : \"").append(mutator.getName())
      .append("\", \"mdesc\" : \"").append(mutator.getDescription()).append("\", \"sourceFile\" : \"").append(sourceFile).append("\"  }");
    return builder.toString();
  }

}
