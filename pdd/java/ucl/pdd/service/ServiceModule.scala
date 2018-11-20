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
import org.joda.time.DateTimeZone

/**
 * Guice module providing business services.
 */
object ServiceModule extends TwitterModule {
  private[this] val timezoneFlag = flag("api.timezone", "Europe/London", "Reference timezone")
  private[this] val testingModeFlag = flag(
    "api.testing_mode",
    false,
    "Whether to switch the server to testing mode (where days only last 5 minutes). It should be only activated for testing purposes.")
  private[this] val geocoderFlag = flag[String]("geocoder.type", "Which geocoder to use to map IP addresses to a country code")

  override def configure(): Unit = {
    bind[DateTimeZone].annotatedWith[Timezone].toInstance(DateTimeZone.forID(timezoneFlag()))
    bind[Boolean].annotatedWith[TestingMode].toInstance(testingModeFlag())
    bind[Duration].annotatedWith[PruneThreshold].toInstance(15.days)
    if (testingModeFlag()) {
      logger.warn("Running in TESTING mode. Days will only last 5 minutes!")
    }

    geocoderFlag.get match {
      case Some("maxmind") => bind[Geocoder].to[MaxmindGeocoder]
      case Some(invalid) => throw new IllegalArgumentException(s"Invalid geocoder type: $invalid")
      case None => bind[Geocoder].toInstance(NullGeocoder)
    }
  }

  @Provides
  def providesTimer: Timer = new JavaTimer

  override def singletonStartup(injector: Injector): Unit = {
    Await.ready(injector.instance[CronManager].startUp())
  }

  override def singletonShutdown(injector: Injector): Unit = {
    Await.ready(injector.instance[CronManager].shutDown())
    Await.ready(injector.instance[Geocoder].close())
  }
}
