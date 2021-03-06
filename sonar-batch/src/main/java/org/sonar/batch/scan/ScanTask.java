/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.batch.scan;

import org.sonar.batch.scan2.ProjectScanContainer;

import org.sonar.api.CoreProperties;
import org.sonar.api.platform.ComponentContainer;
import org.sonar.api.task.Task;
import org.sonar.api.task.TaskDefinition;
import org.sonar.batch.DefaultProjectTree;
import org.sonar.batch.bootstrap.BootstrapProperties;
import org.sonar.batch.bootstrap.TaskContainer;
import org.sonar.batch.phases.Phases;

public class ScanTask implements Task {
  public static final TaskDefinition DEFINITION = TaskDefinition.builder()
    .description("Scan project")
    .key(CoreProperties.SCAN_TASK)
    .taskClass(ScanTask.class)
    .build();

  private final ComponentContainer taskContainer;

  public ScanTask(TaskContainer taskContainer) {
    this.taskContainer = taskContainer;
  }

  public void execute() {
    boolean sensorMode = CoreProperties.ANALYSIS_MODE_SENSOR.equals(taskContainer.getComponentByType(BootstrapProperties.class).property(CoreProperties.ANALYSIS_MODE));
    if (sensorMode) {
      new ProjectScanContainer(taskContainer).execute();
    } else {
      scan(new org.sonar.batch.scan.ProjectScanContainer(taskContainer));
    }
  }

  // Add components specific to project scan (views will use different ones)
  void scan(ComponentContainer scanContainer) {
    scanContainer.add(
      new Phases().enable(Phases.Phase.values()),
      DefaultProjectTree.class,
      ProjectExclusions.class,
      ProjectReactorValidator.class,
      ProjectReactorReady.class,
      DefaultSensorMatcher.class);
    scanContainer.execute();
  }
}
