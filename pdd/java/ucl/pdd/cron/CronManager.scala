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

import com.github.nscala_time.time.Imports._
import com.google.common.util.concurrent.AbstractIdleService
import com.google.inject.{Inject, Singleton}
import org.quartz._
import ucl.pdd.config.Timezone

@Singleton
final class CronManager @Inject()(scheduler: Scheduler, @Timezone timezone: DateTimeZone)
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
      .withSchedule(SimpleScheduleBuilder.simpleSchedule.withIntervalInHours(24).repeatForever())
      .build
    scheduler.scheduleJob(jobDetail, trigger)
  }
}
