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
package org.sonar.server.qualityprofile;

import com.google.common.collect.Maps;
import org.sonar.api.rule.RuleKey;
import org.sonar.core.activity.Activity;
import org.sonar.server.activity.index.ActivityDoc;
import org.sonar.server.activity.index.ActivityNormalizer;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.util.Map;

/**
 * @since 4.4
 */
public class QProfileActivity extends ActivityDoc implements Activity {

  private String ruleName = "Not Yet Implemented";
  private String authorName = "Not Yet Implemented";

  protected QProfileActivity(Map<String, Object> fields) {
    super(fields);
    Map<String, String> details = getField("details");
    for (Map.Entry detail : details.entrySet()) {
      fields.put((String) detail.getKey(), detail.getValue());
    }
    if (!fields.containsKey("severity")) {
      fields.put("severity", null);
    }
  }

  @CheckForNull
  public String ruleName() {
    return ruleName;
  }

  public void ruleName(@Nullable String ruleName) {
    this.ruleName = ruleName;
  }

  @CheckForNull
  public String authorName() {
    return authorName;
  }

  public void authorName(@Nullable String authorName) {
    this.authorName = authorName;
  }

  public String profileKey(){
    return getField("profileKey");
  }

  public RuleKey ruleKey(){
    return RuleKey.parse((String) getField("ruleKey"));
  }

  @CheckForNull
  public String login() {
    return getNullableField(ActivityNormalizer.LogFields.LOGIN.field());
  }

  @CheckForNull
  public String severity(){
    return (String) getNullableField("severity");
  }

  public Map<String, String> parameters() {
    Map<String, String> params = Maps.newHashMap();
    for (String key : fields.keySet()) {
      if (key.startsWith("param_")) {
        params.put(key.replace("param_", ""), (String) fields.get(key));
      }
    }
    return params;
  }

}
