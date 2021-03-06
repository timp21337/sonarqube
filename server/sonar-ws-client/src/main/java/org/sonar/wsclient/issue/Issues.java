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
package org.sonar.wsclient.issue;

import org.sonar.wsclient.base.Paging;
import org.sonar.wsclient.component.Component;
import org.sonar.wsclient.rule.Rule;
import org.sonar.wsclient.user.User;

import javax.annotation.CheckForNull;

import java.util.Collection;
import java.util.List;

/**
 * @since 3.6
 */
public interface Issues {
  List<Issue> list();

  int size();

  Collection<Rule> rules();

  Rule rule(Issue issue);

  Collection<User> users();

  @CheckForNull
  User user(String login);

  Collection<Component> components();

  @CheckForNull
  Component component(Issue issue);

  @CheckForNull
  Component componentById(long id);

  @CheckForNull
  Component componentByKey(String key);

  Collection<Component> projects();

  @CheckForNull
  Component project(Issue issue);

  Collection<ActionPlan> actionPlans();

  @CheckForNull
  ActionPlan actionPlans(Issue issue);

  Paging paging();

  Boolean maxResultsReached();

}
