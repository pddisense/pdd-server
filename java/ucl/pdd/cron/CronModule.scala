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

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.{Injector, TwitterModule}
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.spi.JobFactory

object CronModule extends TwitterModule {
  override def configure(): Unit = {
    bind[JobFactory].to[GuiceJobFactory]
  }

  @Provides
  @Singleton
  def providesScheduler(jobFactory: JobFactory): Scheduler = {
    val scheduler = StdSchedulerFactory.getDefaultScheduler
    scheduler.setJobFactory(jobFactory)
    scheduler
  }

  override def singletonStartup(injector: Injector): Unit = {
    injector.instance[CronManager].startAsync().awaitRunning()
  }

  override def singletonShutdown(injector: Injector): Unit = {
    injector.instance[CronManager].stopAsync().awaitTerminated()
  }
}
