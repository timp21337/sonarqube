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
package org.sonar.api.batch.fs.internal;

import org.sonar.api.batch.fs.InputDir;
import org.sonar.api.utils.PathUtils;

import javax.annotation.CheckForNull;

import java.io.File;
import java.io.Serializable;

/**
 * @since 4.5
 */
public class DefaultInputDir implements InputDir, Serializable {

  private final String relativePath;
  private String absolutePath;
  private String key;

  public DefaultInputDir(String relativePath) {
    this.relativePath = PathUtils.sanitize(relativePath);
  }

  @Override
  public String relativePath() {
    return relativePath;
  }

  /**
   * Marked as nullable just for the unit tests that do not call {@link #setFile(java.io.File)}
   * previously.
   */
  @Override
  @CheckForNull
  public String absolutePath() {
    return absolutePath;
  }

  @Override
  public File file() {
    if (absolutePath == null) {
      throw new IllegalStateException("Can not return the java.io.File because absolute path is not set (see method setFile(java.io.File))");
    }
    return new File(absolutePath);
  }

  /**
   * Component key. It's marked as nullable just for the unit tests that
   * do not previously call {@link #setKey(String)}.
   */
  @CheckForNull
  public String key() {
    return key;
  }

  public DefaultInputDir setAbsolutePath(String s) {
    this.absolutePath = PathUtils.sanitize(s);
    return this;
  }

  public DefaultInputDir setFile(File file) {
    setAbsolutePath(file.getAbsolutePath());
    return this;
  }

  public DefaultInputDir setKey(String s) {
    this.key = s;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DefaultInputDir)) {
      return false;
    }

    DefaultInputDir that = (DefaultInputDir) o;
    return relativePath.equals(that.relativePath);
  }

  @Override
  public int hashCode() {
    return relativePath.hashCode();
  }

  @Override
  public String toString() {
    return "[relative=" + relativePath + ", abs=" + absolutePath + "]";
  }
}
