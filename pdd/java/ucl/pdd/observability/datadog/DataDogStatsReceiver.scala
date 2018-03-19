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

package ucl.pdd.observability.datadog

import java.util.concurrent.ConcurrentHashMap

import com.timgroup.statsd.{NonBlockingStatsDClient, StatsDClient}
import com.twitter.finagle.stats._

import scala.collection.JavaConverters._

final class DataDogStatsReceiver(client: StatsDClient) extends StatsReceiver {
  private[this] val verbosity = new ConcurrentHashMap[Seq[String], Verbosity].asScala
  private[this] val gauges = new ConcurrentHashMap[Seq[String], () => Float].asScala

  /**
   * Constructor without argument, to be loadable as a service.
   */
  def this() = this(new NonBlockingStatsDClient(DataDogStatsReceiver.prefix, "localhost", 8125, DataDogStatsReceiver.constantTags: _*))

  override def repr: DataDogStatsReceiver = this

  override def counter(v: Verbosity, name: String*): Counter = new Counter {
    verbosity += name -> v

    override def incr(delta: Long): Unit = client.incrementCounter(toMetricName(name))

    override def toString: String = s"Counter(${name.mkString("/")})"
  }

  override def stat(v: Verbosity, name: String*): Stat = new Stat {
    verbosity += name -> v

    override def add(value: Float): Unit = client.recordHistogramValue(toMetricName(name), value)

    override def toString: String = s"Stat(${name.mkString("/")})"
  }

  override def addGauge(v: Verbosity, name: String*)(f: => Float): Gauge = new Gauge {
    gauges += name -> (() => f)
    verbosity += name -> v
    client.recordGaugeValue(toMetricName(name), f)

    override def remove(): Unit = gauges -= name

    override def toString: String = s"Gauge(${name.mkString("/")})"
  }

  private def toMetricName(name: Seq[String]) = name.map(_.replaceAll("[^\\w]", "_")).mkString(".")
}

object DataDogStatsReceiver {
  private def prefix = s"app.${sys.env.getOrElse("ROLE", "pdd")}"

  private def constantTags = Seq(s"environment:${sys.env.getOrElse("ENVIRONMENT", "devel")}")
}
