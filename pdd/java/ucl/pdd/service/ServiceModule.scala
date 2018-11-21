/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.service

import com.google.inject.Provides
import com.twitter.conversions.time._
import com.twitter.inject.{Injector, TwitterModule}
import com.twitter.util.{Await, Duration, JavaTimer, Timer}
import org.joda.time.{DateTimeUtils, DateTimeZone}

/**
 * Guice module providing business services.
 */
object ServiceModule extends TwitterModule {
  private val timezoneFlag = flag("timezone", "Europe/London", "Reference timezone")
  private val testingModeFlag = flag(
    "testing_mode",
    false,
    "Whether to switch the server to testing mode (where days last 5 minutes)." +
      "It should be only activated for testing purposes.")

  override def configure(): Unit = {
    DateTimeZone.setDefault(DateTimeZone.forID(timezoneFlag()))
    bind[Duration].annotatedWith[PruneThreshold].toInstance(15.days)

    if (testingModeFlag()) {
      logger.warn("Running in TESTING mode. Days will only last 5 minutes!")
      DateTimeUtils.setCurrentMillisProvider(new TestingModeMillisProvider(System.currentTimeMillis()))
    }
  }

  @Provides
  def providesTimer: Timer = new JavaTimer

  override def singletonStartup(injector: Injector): Unit = {
    Await.ready(injector.instance[CronManager].startUp())
  }

  override def singletonShutdown(injector: Injector): Unit = {
    Await.ready(injector.instance[CronManager].shutDown())
  }

  private class TestingModeMillisProvider(startMillis: Long) extends DateTimeUtils.MillisProvider {
    override def getMillis: Long = {
      // In testing mode we accelerate time, with days lasting only 5 minutes,
      // which is equivalent to every second being being worth 288 seconds of
      // "actual" time.
      // 288 = 24 * 3600 (1 day in seconds) / 300 (5 minutes in seconds)
      val currentMillis = System.currentTimeMillis()
      startMillis + ((currentMillis - startMillis) * 288000)
    }
  }

}
