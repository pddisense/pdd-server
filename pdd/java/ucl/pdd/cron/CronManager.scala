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

package ucl.pdd.cron

import java.util.concurrent.TimeUnit

import com.google.inject.Singleton
import com.twitter.inject.Injector
import com.twitter.util.{Duration, Future, Time, Timer}
import org.joda.time.{DateTime, DateTimeZone, Instant, ReadableInstant}
import ucl.pdd.config.Timezone
import ucl.pdd.util.Service

@Singleton
final class CronManager(timer: Timer, @Timezone timezone: DateTimeZone, injector: Injector)
  extends Service {

  import CronManager._

  override def startUp(): Future[Unit] = Future {
    val nextDay = DateTime.now(timezone).plusDays(1).withTimeAtStartOfDay
    timer.schedule(nextDay.plusHours(1), Duration.fromTimeUnit(1, TimeUnit.DAYS)) {
      injector.instance[CreateSketchesJob].execute(Instant.now())
    }
    timer.schedule(nextDay.plusMinutes(15), Duration.fromTimeUnit(1, TimeUnit.DAYS)) {
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
