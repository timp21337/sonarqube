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
package org.sonar.batch.bootstrap;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.config.PropertyDefinitions;
import org.sonar.batch.protocol.input.GlobalReferentials;

import java.util.Collections;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class GlobalSettingsTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  GlobalReferentials globalRef;
  ProjectDefinition project = ProjectDefinition.create().setKey("struts");
  Configuration deprecatedConf = new BaseConfiguration();
  BootstrapProperties bootstrapProps;

  private AnalysisMode mode;

  @Before
  public void prepare() {
    globalRef = new GlobalReferentials();
    bootstrapProps = new BootstrapProperties(Collections.<String, String>emptyMap());
    mode = mock(AnalysisMode.class);
  }

  @Test
  public void should_load_global_settings() {
    globalRef.globalSettings().put("sonar.cpd.cross", "true");

    GlobalSettings batchSettings = new GlobalSettings(bootstrapProps, new PropertyDefinitions(), globalRef, deprecatedConf, mode);

    assertThat(batchSettings.getBoolean("sonar.cpd.cross")).isTrue();
  }
}
