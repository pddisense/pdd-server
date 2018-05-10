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

import com.google.inject.{Inject, Singleton}
import com.twitter.conversions.time._
import com.twitter.inject.Injector
import com.twitter.util.{Future, Time, Timer}
import org.joda.time.{DateTime, DateTimeZone, Instant, ReadableInstant}
import ucl.pdd.util.Service

/**
 * Register and launch cron jobs.
 *
 * @param timer       Timer (owned by the cron manager).
 * @param injector    Guice injector.
 * @param timezone    Current timezone.
 * @param testingMode Is the testing mode enabled?
 */
@Singleton
final class CronManager @Inject()(
  timer: Timer,
  injector: Injector,
  @Timezone timezone: DateTimeZone,
  @TestingMode testingMode: Boolean)
  extends Service {

  import CronManager._

  override def startUp(): Future[Unit] = Future {
    // In testing mode, jobs run every five minutes, and start 1 minute after launching the application.
    // In production mode, jobs run every day, and start a few hours after launching the application.
    val period = if (testingMode) 5.minutes else 1.day
    val nextDay = if (testingMode) DateTime.now(timezone) else DateTime.now(timezone).plusDays(1).withTimeAtStartOfDay

    timer.schedule(if (testingMode) nextDay.plusMinutes(1) else nextDay.plusHours(1), period) {
      injector.instance[CreateSketchesJob].execute(Instant.now())
    }

    timer.schedule(if (testingMode) nextDay.plusMinutes(1) else nextDay.plusMinutes(30), period) {
      injector.instance[AggregateSketchesJob].execute(Instant.now())
    }
  }

  override def shutDown(): Future[Unit] = Future {
    timer.stop()
  }
}

object CronManager {
  implicit def instantToTime(instant: ReadableInstant): Time = Time.fromMilliseconds(instant.getMillis)
}
