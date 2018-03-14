/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.cron

import com.github.nscala_time.time.Imports._
import com.google.common.util.concurrent.AbstractIdleService
import com.google.inject.{Inject, Singleton}
import org.quartz._
import ucl.pdd.config.{DayDuration, Timezone}

@Singleton
final class CronManager @Inject()(
  scheduler: Scheduler,
  @DayDuration dayDuration: Duration,
  @Timezone timezone: DateTimeZone)
  extends AbstractIdleService {

  override protected def startUp(): Unit = {
    scheduler.start()
    scheduleDaily(scheduler, classOf[AggregateSketchesJob], 0)
    scheduleDaily(scheduler, classOf[CreateSketchesJob], 30)
  }

  override protected def shutDown(): Unit = {
    scheduler.shutdown()
  }

  private def scheduleDaily(scheduler: Scheduler, jobClass: Class[_ <: Job], minute: Int): Unit = {
    val jobDetail = JobBuilder
      .newJob(jobClass)
      .withIdentity(jobClass.getSimpleName, "PddJob")
      .build
    val trigger = TriggerBuilder
      .newTrigger
      .withIdentity(jobClass.getSimpleName + "Trigger")
      .startAt(DateBuilder.tomorrowAt(0, minute, 0))
      .withSchedule(
        SimpleScheduleBuilder
          .simpleSchedule
          .withIntervalInMilliseconds(dayDuration.millis)
          .repeatForever())
      .build
    scheduler.scheduleJob(jobDetail, trigger)
  }
}
