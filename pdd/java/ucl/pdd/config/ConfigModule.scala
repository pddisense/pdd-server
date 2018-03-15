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

package ucl.pdd.config

import java.util.concurrent.TimeUnit

import com.twitter.inject.TwitterModule
import com.twitter.util.{Duration => TwitterDuration}
import org.joda.time.{DateTimeZone, Duration}

import scala.util.Random

object ConfigModule extends TwitterModule {
  private[this] val dayDurationFlag = flag("api.day_duration", TwitterDuration.fromTimeUnit(1, TimeUnit.DAYS), "Duration of one day")
  private[this] val timezoneFlag = flag("api.timezone", "Europe/London", "Reference timezone")
  private[this] val tokenFlag = flag[String]("api.access_token", "Token used to secure the access to relevant endpoints")

  override def configure(): Unit = {
    bind[Duration].annotatedWith[DayDuration].toInstance(new Duration(dayDurationFlag().inMillis))
    bind[DateTimeZone].annotatedWith[Timezone].toInstance(DateTimeZone.forID(timezoneFlag()))
    bind[String].annotatedWith[AccessToken].toInstance(tokenFlag.get.getOrElse(randomToken(20)))
  }

  private[this] val characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

  private def randomToken(length: Int): String = {
    val token = Seq.fill(length)(characters(Random.nextInt(characters.length))).mkString
    logger.info("---------------------------------------------------------")
    logger.info(s"Randomly generated API access token: $token")
    logger.info("---------------------------------------------------------")
    token
  }
}
