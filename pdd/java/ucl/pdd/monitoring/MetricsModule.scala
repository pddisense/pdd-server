/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.monitoring

import com.google.inject.{Provides, Singleton}
import com.timgroup.statsd.NonBlockingStatsDClient
import com.twitter.finagle.stats.{BroadcastStatsReceiver, MetricsStatsReceiver, StatsReceiver}
import com.twitter.inject.TwitterModule
import com.twitter.util.JavaTimer

import scala.collection.mutable

/**
 * Guice module configuring the metrics provider.
 */
object MetricsModule extends TwitterModule {
  // DataDog options.
  private val datadogServerFlag = flag[String]("datadog_server", "Address to the DataDog agent.")

  @Provides
  @Singleton
  def providesStatsReceiver(): StatsReceiver = {
    val receivers = mutable.ListBuffer.empty[StatsReceiver]

    // The default stats receiver is always configured. It allows viewing the metrics
    // through the admin HTTP server.
    receivers += new MetricsStatsReceiver()

    // Configure Datadog.
    datadogServerFlag.get.foreach { datadogServer =>
      val (host, port) = datadogServer.split(":") match {
        case Array(h, p) => (h, p.toInt)
        case invalid => throw new IllegalArgumentException(s"Invalid Datadog address: $invalid")
      }
      // We use the ENVIRONMENT environment variable (instead of a flag), because
      // the former is already used to parametrise Sentry.
      val constantTags = Seq(s"environment:${sys.env.getOrElse("ENVIRONMENT", "devel")}")
      val client = new NonBlockingStatsDClient("app", host, port, constantTags: _*)
      receivers += new DataDogStatsReceiver(client, new JavaTimer)
    }

    BroadcastStatsReceiver(receivers)
  }
}
