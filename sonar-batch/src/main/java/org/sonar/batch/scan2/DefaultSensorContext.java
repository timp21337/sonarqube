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
package org.sonar.batch.scan2;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.internal.DefaultActiveRule;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.duplication.DuplicationBuilder;
import org.sonar.api.batch.sensor.duplication.DuplicationGroup;
import org.sonar.api.batch.sensor.duplication.DuplicationTokenBuilder;
import org.sonar.api.batch.sensor.duplication.internal.DefaultDuplicationBuilder;
import org.sonar.api.batch.sensor.highlighting.HighlightingBuilder;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueBuilder;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssue;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueBuilder;
import org.sonar.api.batch.sensor.measure.Measure;
import org.sonar.api.batch.sensor.measure.MeasureBuilder;
import org.sonar.api.batch.sensor.measure.internal.DefaultMeasure;
import org.sonar.api.batch.sensor.measure.internal.DefaultMeasureBuilder;
import org.sonar.api.batch.sensor.symbol.SymbolTableBuilder;
import org.sonar.api.config.Settings;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.MessageException;
import org.sonar.batch.duplication.BlockCache;
import org.sonar.batch.duplication.DefaultTokenBuilder;
import org.sonar.batch.duplication.DuplicationCache;
import org.sonar.batch.highlighting.DefaultHighlightingBuilder;
import org.sonar.batch.index.ComponentDataCache;
import org.sonar.batch.issue.IssueFilters;
import org.sonar.batch.scan.SensorContextAdaptor;
import org.sonar.batch.symbol.DefaultSymbolTableBuilder;
import org.sonar.core.component.ComponentKeys;
import org.sonar.duplications.internal.pmd.PmdBlockChunker;

import java.io.Serializable;
import java.util.List;

public class DefaultSensorContext implements SensorContext {

  private final AnalyzerMeasureCache measureCache;
  private final AnalyzerIssueCache issueCache;
  private final ProjectDefinition def;
  private final Settings settings;
  private final FileSystem fs;
  private final ActiveRules activeRules;
  private final IssueFilters issueFilters;
  private final ComponentDataCache componentDataCache;
  private final BlockCache blockCache;
  private final DuplicationCache duplicationCache;

  public DefaultSensorContext(ProjectDefinition def, AnalyzerMeasureCache measureCache, AnalyzerIssueCache issueCache,
    Settings settings, FileSystem fs, ActiveRules activeRules, IssueFilters issueFilters, ComponentDataCache componentDataCache,
    BlockCache blockCache, DuplicationCache duplicationCache) {
    this.def = def;
    this.measureCache = measureCache;
    this.issueCache = issueCache;
    this.settings = settings;
    this.fs = fs;
    this.activeRules = activeRules;
    this.issueFilters = issueFilters;
    this.componentDataCache = componentDataCache;
    this.blockCache = blockCache;
    this.duplicationCache = duplicationCache;
  }

  @Override
  public Settings settings() {
    return settings;
  }

  @Override
  public FileSystem fileSystem() {
    return fs;
  }

  @Override
  public ActiveRules activeRules() {
    return activeRules;
  }

  @Override
  public <G extends Serializable> MeasureBuilder<G> measureBuilder() {
    return new DefaultMeasureBuilder<G>();
  }

  @Override
  public Measure getMeasure(String metricKey) {
    return measureCache.byMetric(def.getKey(), def.getKey(), metricKey);
  }

  @Override
  public <G extends Serializable> Measure<G> getMeasure(Metric<G> metric) {
    return (Measure<G>) measureCache.byMetric(def.getKey(), def.getKey(), metric.key());
  }

  @Override
  public Measure getMeasure(InputFile file, String metricKey) {
    return measureCache.byMetric(def.getKey(), ComponentKeys.createEffectiveKey(def.getKey(), file), metricKey);
  }

  @Override
  public <G extends Serializable> Measure<G> getMeasure(InputFile file, Metric<G> metric) {
    return (Measure<G>) measureCache.byMetric(def.getKey(), ComponentKeys.createEffectiveKey(def.getKey(), file), metric.key());
  }

  @Override
  public void addMeasure(Measure<?> measure) {
    InputFile inputFile = measure.inputFile();
    if (inputFile != null) {
      measureCache.put(def.getKey(), ComponentKeys.createEffectiveKey(def.getKey(), inputFile), (DefaultMeasure) measure);
    } else {
      measureCache.put(def.getKey(), def.getKey(), (DefaultMeasure) measure);
    }
  }

  @Override
  public IssueBuilder issueBuilder() {
    return new DefaultIssueBuilder();
  }

  @Override
  public boolean addIssue(Issue issue) {
    String resourceKey;
    if (issue.inputPath() != null) {
      resourceKey = ComponentKeys.createEffectiveKey(def.getKey(), issue.inputPath());
    } else {
      resourceKey = def.getKey();
    }
    RuleKey ruleKey = issue.ruleKey();
    DefaultActiveRule activeRule = (DefaultActiveRule) activeRules.find(ruleKey);
    if (activeRule == null) {
      // rule does not exist or is not enabled -> ignore the issue
      return false;
    }
    if (Strings.isNullOrEmpty(activeRule.name()) && Strings.isNullOrEmpty(issue.message())) {
      throw MessageException.of(String.format("The rule '%s' has no name and the related issue has no message.", ruleKey));
    }

    updateIssue((DefaultIssue) issue, activeRule);

    if (issueFilters.accept(SensorContextAdaptor.toDefaultIssue(def.getKey(), resourceKey, issue), null)) {
      issueCache.put(def.getKey(), resourceKey, (DefaultIssue) issue);
      return true;
    }

    return false;
  }

  private void updateIssue(DefaultIssue issue, DefaultActiveRule activeRule) {
    if (Strings.isNullOrEmpty(issue.message())) {
      issue.setMessage(activeRule.name());
    }

    if (issue.severity() == null) {
      issue.setSeverity(activeRule.severity());
    }
  }

  @Override
  public HighlightingBuilder highlightingBuilder(InputFile inputFile) {
    return new DefaultHighlightingBuilder(((DefaultInputFile) inputFile).key(), componentDataCache);
  }

  @Override
  public SymbolTableBuilder symbolTableBuilder(InputFile inputFile) {
    return new DefaultSymbolTableBuilder(((DefaultInputFile) inputFile).key(), componentDataCache);
  }

  @Override
  public DuplicationTokenBuilder duplicationTokenBuilder(InputFile inputFile) {
    PmdBlockChunker blockChunker = new PmdBlockChunker(getBlockSize(inputFile.language()));

    return new DefaultTokenBuilder(inputFile, blockCache, blockChunker);
  }

  @Override
  public DuplicationBuilder duplicationBuilder(InputFile inputFile) {
    return new DefaultDuplicationBuilder(inputFile);
  }

  @Override
  public void saveDuplications(InputFile inputFile, List<DuplicationGroup> duplications) {
    Preconditions.checkState(duplications.size() > 0, "Empty duplications");
    String effectiveKey = ((DefaultInputFile) inputFile).key();
    for (DuplicationGroup duplicationGroup : duplications) {
      Preconditions.checkState(effectiveKey.equals(duplicationGroup.originBlock().resourceKey()), "Invalid duplication group");
    }
    duplicationCache.put(effectiveKey, duplications);
  }

  private int getBlockSize(String languageKey) {
    int blockSize = settings.getInt("sonar.cpd." + languageKey + ".minimumLines");
    if (blockSize == 0) {
      blockSize = getDefaultBlockSize(languageKey);
    }
    return blockSize;
  }

  private static int getDefaultBlockSize(String languageKey) {
    if ("cobol".equals(languageKey)) {
      return 30;
    } else if ("abap".equals(languageKey) || "natur".equals(languageKey)) {
      return 20;
    } else {
      return 10;
    }
  }

}
