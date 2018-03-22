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

package ucl.pdd.metrics

import com.google.inject.{Provider, Singleton}
import com.timgroup.statsd.NonBlockingStatsDClient
import com.twitter.finagle.stats.{MetricsStatsReceiver, StatsReceiver}
import com.twitter.inject.TwitterModule
import com.twitter.util.JavaTimer

object MetricsModule extends TwitterModule {
  private[this] val typeFlag = flag("metrics.type", "finagle", "Metrics type ('finagle' or 'datadog')")

  // DataDog options.
  private[this] val datadogServerFlag = flag("metrics.datadog.server", "127.0.0.1:8125", "Address to DataDog agent")

  override def configure(): Unit = {
    typeFlag() match {
      case "finagle" => bind[StatsReceiver].toProvider[MetricsStatsReceiverProvider].in[Singleton]
      case "datadog" => bind[StatsReceiver].toProvider[DataDogStatsReceiverProvider].in[Singleton]
      case invalid => throw new IllegalArgumentException(s"Invalid metrics type: $invalid")
    }
  }

  private class MetricsStatsReceiverProvider extends Provider[StatsReceiver] {
    override def get(): StatsReceiver = new MetricsStatsReceiver()
  }

  private class DataDogStatsReceiverProvider extends Provider[StatsReceiver] {
    override def get(): StatsReceiver = {
      val (host, port) = datadogServerFlag().split(":") match {
        case Array(h, p) => (h, p.toInt)
        case invalid => throw new IllegalArgumentException(s"Invalid server address: $invalid")
      }
      val constantTags = Seq(s"environment:${sys.env.getOrElse("ENVIRONMENT", "devel")}") ++
        sys.env.get("SERVICE").map(service => s"service:$service").toSeq
      val client = new NonBlockingStatsDClient("app", host, port, constantTags: _*)
      new DataDogStatsReceiver(client, new JavaTimer)
    }
  }

}
