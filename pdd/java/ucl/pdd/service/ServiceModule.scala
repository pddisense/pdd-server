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

import com.google.inject.Provides
import com.twitter.inject.{Injector, TwitterModule}
import com.twitter.util.{Await, JavaTimer, Timer}
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

  override def configure(): Unit = {
    bind[DateTimeZone].annotatedWith[Timezone].toInstance(DateTimeZone.forID(timezoneFlag()))
    bind[Boolean].annotatedWith[TestingMode].toInstance(testingModeFlag())
    if (testingModeFlag()) {
      logger.warn("Running in TESTING mode. Days will only last 5 minutes!")
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
}
