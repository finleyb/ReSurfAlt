/*
 * Copyright (C) 2016 Original Work Benjamin Finley
 *
 * This file is part of ReSurfAlt.
 *
 * ReSurfAlt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReSurfAlt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ReSurfAlt. If not, see <http://www.gnu.org/licenses/>.
 */
package com.resurf.strategies

import com.twitter.util.{Time}
import com.resurf.common.WebRequest

/**
 * This trait provides a representation of a specific strategy for identifying user generated requests
 */
trait Strategy {

  /**
   * Get the browsing behavior of the user according to the strategy
   *
   * @return the browsing of the user according to the strategy
   */
  def getUserBrowsing: Seq[Tuple2[Time,String]]
}