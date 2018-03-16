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

import java.util.Date

import org.joda.time.Instant
import org.quartz._

import scala.collection.mutable

final class FakeJobExecutionContext(at: Instant) extends JobExecutionContext {
  private[this] val data = mutable.Map.empty[AnyRef, AnyRef]
  private[this] var result: AnyRef = null

  override def getFireInstanceId: String = "fake-instance-id"

  override def setResult(value: AnyRef): Unit = result = value

  override def getCalendar: Calendar = ???

  override def put(key: AnyRef, value: AnyRef): Unit = data.put(key, value)

  override def getMergedJobDataMap: JobDataMap = ???

  override def getJobRunTime: Long = ???

  override def get(key: AnyRef): AnyRef = data.get(key).orNull

  override def getScheduler: Scheduler = ???

  override def getNextFireTime: Date = null

  override def getTrigger: Trigger = ???

  override def getScheduledFireTime: Date = new Date(at.getMillis)

  override def getFireTime: Date = new Date(at.getMillis)

  override def getPreviousFireTime: Date = null

  override def isRecovering: Boolean = false

  override def getJobDetail: JobDetail = ???

  override def getResult: AnyRef = result

  override def getRefireCount: Int = 0

  override def getRecoveringTriggerKey: TriggerKey = ???

  override def getJobInstance: Job = ???
}
