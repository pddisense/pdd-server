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

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.{Injector, TwitterModule}
import com.twitter.util.Await
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
    Await.ready(injector.instance[CronManager].startUp())
  }

  override def singletonShutdown(injector: Injector): Unit = {
    Await.ready(injector.instance[CronManager].shutDown())
  }
}
