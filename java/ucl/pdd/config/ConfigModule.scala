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

package ucl.pdd.config

import java.util.concurrent.TimeUnit

import com.twitter.inject.TwitterModule
import com.twitter.util.{Duration => TwitterDuration}
import org.joda.time.{DateTimeZone, Duration}

object ConfigModule extends TwitterModule {
  private[this] val dayDurationFlag = flag("day_duration", TwitterDuration.fromTimeUnit(1, TimeUnit.DAYS), "Duration of one day")
  private[this] val timezoneFlag = flag("timezone", "Europe/London", "Reference timezone")

  override def configure(): Unit = {
    bind[Duration].annotatedWith[DayDuration].toInstance(new Duration(dayDurationFlag().inMillis))
    bind[DateTimeZone].annotatedWith[Timezone].toInstance(DateTimeZone.forID(timezoneFlag()))
  }
}
