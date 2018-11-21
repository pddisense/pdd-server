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

package ucl.pdd.metrics

import com.google.inject.{Provider, Singleton}
import com.timgroup.statsd.NonBlockingStatsDClient
import com.twitter.finagle.stats.{MetricsStatsReceiver, StatsReceiver}
import com.twitter.inject.TwitterModule
import com.twitter.util.JavaTimer

/**
 * Guice module configuring the metrics provider.
 */
object MetricsModule extends TwitterModule {
  private val typeFlag = flag(
    "metrics",
    "finagle",
    "Where to write metrics. Valid values are: 'finagle', 'datadog'.")

  // DataDog options.
  private val datadogServerFlag = flag(
    "metrics.datadog.server",
    "127.0.0.1:8125",
    "Address to the DataDog agent.")

  override def configure(): Unit = {
    typeFlag() match {
      case "finagle" => bind[StatsReceiver].toProvider[FinagleStatsReceiverProvider].in[Singleton]
      case "datadog" => bind[StatsReceiver].toProvider[DataDogStatsReceiverProvider].in[Singleton]
      case invalid => throw new IllegalArgumentException(s"Invalid metrics type: $invalid")
    }
  }

  private class FinagleStatsReceiverProvider extends Provider[StatsReceiver] {
    override def get(): StatsReceiver = new MetricsStatsReceiver()
  }

  private class DataDogStatsReceiverProvider extends Provider[StatsReceiver] {
    override def get(): StatsReceiver = {
      val (host, port) = datadogServerFlag().split(":") match {
        case Array(h, p) => (h, p.toInt)
        case invalid => throw new IllegalArgumentException(s"Invalid Datadog server address: $invalid")
      }
      // We use the ENVIRONMENT environment variable (instead of a flag), because the former is
      // already used to parametrise Sentry.
      val constantTags = Seq(s"environment:${sys.env.getOrElse("ENVIRONMENT", "devel")}")
      val client = new NonBlockingStatsDClient("app", host, port, constantTags: _*)
      new DataDogStatsReceiver(client, new JavaTimer)
    }
  }

}
