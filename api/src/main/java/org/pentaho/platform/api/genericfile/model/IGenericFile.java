/*!
 *
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
 *
 * Copyright (c) 2023 Hitachi Vantara. All rights reserved.
 *
 */

package org.pentaho.platform.api.genericfile.model;

import java.util.Date;

public interface IGenericFile extends IProviderable {
  String TYPE_FOLDER = "folder";
  String TYPE_FILE = "file";

  String getName();
  String getPath();
  String getParent();
  String getType();

  default boolean isFolder() {
    return TYPE_FOLDER.equals( getType() );
  }

  String getRoot();
  Date getModifiedDate();
  boolean isCanEdit();
  boolean isCanDelete();

  default IGenericFolder asFolder() {
    return null;
  }

  String getTitle();

  String getDescription();
}