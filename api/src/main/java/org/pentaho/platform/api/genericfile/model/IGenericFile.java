/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2023 - 2024 Hitachi Vantara. All rights reserved.
 */

package org.pentaho.platform.api.genericfile.model;

import java.util.Date;

/**
 * The {@code IGenericFile} interface contains basic information about a generic file.
 * <p>
 * To know whether a file is a file, proper or a folder, use {@link #isFolder()} or {@link #getType()}.
 * Folder generic file instances do not directly contain their children. Children of a folder generic file are
 * represented as part of a {@link IGenericFileTree} instance.
 * <p>
 * To know the name or the path of a file, use {@link #getName()} and {@link #getPath()}, respectively.
 */
public interface IGenericFile extends IProviderable {
  String TYPE_FOLDER = "folder";
  String TYPE_FILE = "file";

  /**
   * Gets the physical name of the file.
   * <p>
   * Generally, the physical name of a file is the last segment of its {@link #getPath() path}.
   * <p>
   * A valid generic file instance must have a non-null name.
   *
   * @see #getTitle()
   */
  String getName();

  /**
   * Gets the path of the file, as a string.
   * <p>
   * A valid generic file instance must have a non-null path.
   * @see #getName()
   * @see org.pentaho.platform.api.genericfile.GenericFilePath
   */
  String getPath();

  /**
   * Gets the path of the parent folder, as a string.
   * <p>
   * Note that the {@link org.pentaho.platform.api.genericfile.GenericFilePath#NULL null folder} does not have a parent.
   * Otherwise, a valid generic file instance must have a non-null parent path.
   *
   * @see #getPath()
   */
  String getParentPath();

  /**
   * Gets the type of generic file, one of: {@link #TYPE_FOLDER} or {@link #TYPE_FILE}.
   * @see #isFolder()
   */
  String getType();

  /**
   * Determines if a generic file is a folder.
   * <p>
   * The default implementation checks if the value of {@link #getType()} is equal to {@link #TYPE_FOLDER}.
   * @return {@code true}, if the generic file is a folder; {@code false}, otherwise.
   */
  default boolean isFolder() {
    return TYPE_FOLDER.equals( getType() );
  }

  /**
   * Gets the modified date of the generic file.
   */
  Date getModifiedDate();

  /**
   * Gets whether the generic file can be edited.
   */
  boolean isCanEdit();

  /**
   * Gets whether the generic file can be deleted.
   */
  boolean isCanDelete();

  /**
   * Gets the title of the file.
   * <p>
   * The title of a file is a localized, human-readable version of its {@link #getName() name}.
   * <p>
   * Unlike the name of a file, the title may not be unique amongst siblings.
   * <p>
   * When title of a file is unspecified, the name of a file can be used in its place.
   *
   * @see #getName()
   * @see #getDescription()
   */
  String getTitle();

  /**
   * Gets the description of the file.
   * <p>
   * The description of a file is a localized, human-readable description of a file. Typically, displayed in a tooltip
   * in a user interface.
   *
   * @see #getName()
   * @see #getTitle()
   */
  String getDescription();
}
