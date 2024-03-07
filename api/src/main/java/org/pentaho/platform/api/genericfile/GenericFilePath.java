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
 * Copyright (c) 2024 Hitachi Vantara. All rights reserved.
 */
package org.pentaho.platform.api.genericfile;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.pentaho.platform.api.genericfile.exception.InvalidPathException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents a <i>Generic File</i> path.
 * <p>
 * Generic file path instances are immutable.
 * <p>
 * To create a generic file path instance from a path string, call {@link #parse(String)}.
 * <p>
 * The <i>null</i> path is a special path which is the parent of the <i>provider root paths</i>.
 * Its string representation is the empty string.
 * The <i>null</i> path instance is a singleton exposed by the {@link #NULL} property.
 */
public class GenericFilePath {
  /**
   * The path separator character, {@code /}.
   */
  public static final String PATH_SEPARATOR = "/";

  private static final Pattern PATH_WITH_SCHEME_PATTERN = Pattern.compile( "^(\\w+://)(.*)$" );

  private static final Pattern PATH_SEPARATOR_SPLIT_PATTERN = Pattern.compile( "\\s*" + PATH_SEPARATOR + "\\s*" );

  private static final String SCHEME_SUFFIX = "://";

  /**
   * The null path singleton instance.
   * <p>
   * The null path has an empty string representation, and has no {@see #getSegments() segments}.
   *
   * @see #NULL
   */
  public static final GenericFilePath NULL = new GenericFilePath( Collections.emptyList() );

  private static final String[] EMPTY_ARRAY = new String[ 0 ];

  @NonNull
  private final String path;

  @NonNull
  private final List<String> segments;

  private GenericFilePath( @NonNull List<String> segments ) {
    this.segments = Collections.unmodifiableList( segments );

    // Rebuild the path to ensure it´s normalized.
    this.path = getRootSegment() + String.join( PATH_SEPARATOR, getNonRootSegments() );
  }

  /**
   * Gets a value that indicates if the path is <i>null</i>.
   *
   * @return {@code true}, if the path is null; {@code false}, otherwise.
   * @see #NULL
   */
  public boolean isNull() {
    return segments.isEmpty();
  }

  /**
   * Gets the path's root segment.
   * <p>
   * The root segment is that which identifies the path's provider.
   * <p>
   * The root segment of the null path, is empty, given that it has no segments, nor corresponds to any provider.
   * <p>
   * The root segment of the special <i>Repository</i> provider is the {@link #PATH_SEPARATOR}.
   * While, other providers have a root segment composed of a scheme/protocol, followed by the {@code ://} suffix.
   * <p>
   * Use {@link #hasScheme()} to determine if a provider has a scheme, and {@link #getScheme()} to extract it from
   * the root segment.
   *
   * @return The root segment.
   */
  @NonNull
  public String getRootSegment() {
    return isNull() ? "" : segments.get( 0 );
  }

  /**
   * Gets the path's non-root segments.
   * <p>
   * The non-root segments are the segments following the {@link #getRootSegment() root segment}.
   * <p>
   * The null path has no segments, root or not.
   *
   * @return The list of non-root segments, possibly empty.
   */
  @NonNull
  public List<String> getNonRootSegments() {
    return isNull() ? Collections.emptyList() : segments.subList( 1, segments.size() );
  }

  /**
   * Gets a value that indicates if the path has a scheme.
   * <p>
   * For more information, see {@link #getRootSegment()}.
   *
   * @return {@code true}, if the path has a scheme; {@code false}, otherwise.
   * @see #getScheme()
   */
  public boolean hasScheme() {
    return !PATH_SEPARATOR.equals( getRootSegment() );
  }

  /**
   * Gets the path's scheme.
   * <p>
   * For more information, see {@link #getRootSegment()}.
   *
   * @return The path's scheme if it has one; {@code null}, if not.
   * @see #hasScheme()
   */
  @Nullable
  public String getScheme() {
    String root = getRootSegment();
    return hasScheme()
      ? root.substring( 0, root.length() - SCHEME_SUFFIX.length() )
      : null;
  }

  /**
   * Gets the path's segments.
   *
   * @return An immutable list of path segments, possibly empty.
   * @see #getRootSegment()
   * @see #getNonRootSegments()
   */
  @NonNull
  public List<String> getSegments() {
    return segments;
  }

  /**
   * Gets the path's normalized string representation.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return path;
  }

  /**
   * Gets the parent generic path instance.
   * <p>
   * The {@link #NULL null path} has no parent.
   * <p>
   * The parent path of the <i>provider root paths</i> is the <i>null</i> path.
   *
   * @return The parent generic path instance, if any; {@link null}, if none.
   */
  @Nullable
  public GenericFilePath getParent() {
    if ( isNull() ) {
      return null;
    }

    if ( segments.size() == 1 ) {
      return NULL;
    }

    return new GenericFilePath( segments.subList( 0, segments.size() - 1 ) );
  }

  /**
   * Parses a given string representation.
   * <p>
   * A {@code null} or empty string representation is parsed as the singleton {@link #NULL null path} instance.
   * <p>
   * The path is otherwise parsed and normalized.
   * The root segment is identified, and other segments are space-trimmed and removed if empty.
   * <p>
   * Segments equal to {@code .} or {@code ..} are not currently being validated or normalized.
   * <p>
   * If the path ends with a {@link #PATH_SEPARATOR} character, it is ignored.
   *
   * @param path The path string to parse, possibly {@code null} or empty.
   * @return The generic path instance.
   * @throws InvalidPathException If the path is invalid. Specifically, if the path's root segment is not either
   *                              a {@link #PATH_SEPARATOR} or a scheme followed by the {@code ://} suffix.
   */
  @NonNull
  public static GenericFilePath parse( @Nullable String path ) throws InvalidPathException {
    if ( path == null ) {
      return NULL;
    }

    String restPath = path.trim();
    if ( restPath.isEmpty() ) {
      return NULL;
    }

    String root;
    if ( restPath.startsWith( PATH_SEPARATOR ) ) {
      root = PATH_SEPARATOR;
      restPath = restPath.substring( PATH_SEPARATOR.length() );
    } else {
      Matcher matcher = PATH_WITH_SCHEME_PATTERN.matcher( restPath );
      if ( !matcher.matches() ) {
        throw new InvalidPathException();
      }

      root = matcher.group( 1 );
      restPath = matcher.group( 2 );
    }

    if ( restPath.endsWith( PATH_SEPARATOR ) ) {
      restPath = restPath.substring( 0, restPath.length() - 1 );
    }

    String[] restSegments = splitPath( restPath );
    List<String> segments = new ArrayList<>( 1 + restSegments.length );
    segments.add( root );
    Collections.addAll( segments, restSegments );

    return new GenericFilePath( segments );
  }

  @NonNull
  private static String[] splitPath( @NonNull String path ) {
    return path.isEmpty()
      ? EMPTY_ARRAY
      : PATH_SEPARATOR_SPLIT_PATTERN.split( path );
  }

  @Override
  public boolean equals( Object other ) {
    if ( this == other ) {
      return true;
    }

    if ( !( other instanceof GenericFilePath ) ) {
      return false;
    }

    GenericFilePath that = (GenericFilePath) other;
    return Objects.equals( path, that.path );
  }

  @Override
  public int hashCode() {
    return Objects.hash( path );
  }

  /**
   * Checks if this path equals, or is an ancestor of, another one.
   *
   * @param other The path to check against.
   * @return {@code true}, if the given path is contained in this one; {@code false}, otherwise.
   */
  public boolean contains( @NonNull GenericFilePath other ) {
    Objects.requireNonNull( other );

    List<String> excess = other.relativeSegments( this );

    // May be empty.
    return excess != null;
  }

  /**
   * Gets the segments of this path relative to a given base path.
   * <p>
   * When this path is not contained in the given base path, {@code null} is returned.
   * Otherwise, a list is returned with the segments of this path not contained in the given base path.
   *
   * @param base The base path.
   * @return A possibly empty segment list, if this path is contained in the given base path; {@code null}, otherwise.
   */
  @Nullable
  public List<String> relativeSegments( @NonNull GenericFilePath base ) {
    Objects.requireNonNull( base );

    // Check for null base upfront. Small optimization.
    if ( base.isNull() ) {
      return segments;
    }

    int baseCount = base.segments.size();
    int count = segments.size();
    if ( baseCount > count ) {
      return null;
    }

    for ( int i = 0; i < baseCount; i++ ) {
      if ( !base.segments.get( i ).equals( segments.get( i ) ) ) {
        return null;
      }
    }

    return segments.subList( baseCount, count );
  }

  /**
   * Builds a child path of this one, given the child segment.
   *
   * @param segment The child segment.
   * @return The child generic path instance.
   * @throws IllegalArgumentException If the given segment is empty, after normalization.
   */
  @NonNull
  public GenericFilePath child( @NonNull String segment ) {
    Objects.requireNonNull( segment );

    String normalizedSegment = segment.trim();
    if ( normalizedSegment.isEmpty() ) {
      throw new IllegalArgumentException( "Path is empty." );
    }

    List<String> childSegments = new ArrayList<>( segments );
    childSegments.add( normalizedSegment );

    return new GenericFilePath( childSegments );
  }
}
