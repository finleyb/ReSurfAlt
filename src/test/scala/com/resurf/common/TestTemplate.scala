/*
 * Copyright (C) 2015 Original Work Marios Iliofotou
 * Copyright (C) 2016 Modified Work Benjamin Finley
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

package com.resurf.common
import org.apache.log4j.{Logger,ConsoleAppender,PatternLayout}
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter
import org.scalatest.Matchers
import org.scalatest.OptionValues._

class TestTemplate extends FunSuite with BeforeAndAfter with Matchers {

  protected def log4jToConsoleAndNewLevel(newLevel: org.apache.log4j.Level) = {
    val rootLogger = org.apache.log4j.Logger.getRootLogger
    rootLogger.removeAllAppenders()
    rootLogger.setLevel(newLevel)
    rootLogger.addAppender(new ConsoleAppender(new PatternLayout("[%d{dd/MM/yy hh:mm:ss:sss z}] %5p %c{2}: %m%n")))
  }

  before {
    //org.apache.log4j.BasicConfigurator.configure()
    log4jToConsoleAndNewLevel(org.apache.log4j.Level.OFF)
  }
}
