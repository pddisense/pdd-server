/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ucl.pdd.service

import com.google.inject.{Inject, Singleton}
import com.twitter.conversions.time._
import com.twitter.inject.Injector
import com.twitter.util.{Future, Time, Timer}
import org.joda.time.{DateTime, DateTimeZone, Instant, ReadableInstant}
import ucl.pdd.config.{TestingMode, Timezone}
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
