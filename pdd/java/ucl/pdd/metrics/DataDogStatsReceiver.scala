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

import java.util.concurrent.ConcurrentHashMap

import com.timgroup.statsd.StatsDClient
import com.twitter.conversions.time._
import com.twitter.finagle.stats._
import com.twitter.util.{Time, Timer}

import scala.collection.JavaConverters._

final class DataDogStatsReceiver(client: StatsDClient, timer: Timer) extends StatsReceiver {
  private[this] val verbosity = new ConcurrentHashMap[Seq[String], Verbosity].asScala
  private[this] val gauges = new ConcurrentHashMap[Seq[String], DataDogGauge].asScala

  // Every 10 seconds, gauges are sampled. We chose a 10 seconds interval because, by default, the
  // DataDog agent sends metrics every 10 seconds.
  timer.schedule(Time.now + 10.seconds, 10.seconds) {
    gauges.values.foreach(gauge => client.recordGaugeValue(gauge.name, gauge(), gauge.tags: _*))
  }

  private object NoopCounter extends Counter {
    override def incr(delta: Long): Unit = {}
  }

  private object NoopGauge extends Gauge {
    override def remove(): Unit = {}
  }

  private object NoopStat extends Stat {
    override def add(value: Float): Unit = {}
  }

  private class DataDogCounter(names: Seq[String]) extends Counter {
    private[this] val (name, tags) = toMetricName(names)

    override def incr(delta: Long): Unit = client.incrementCounter(name, tags: _*)
  }

  private class DataDogStat(names: Seq[String]) extends Stat {
    private[this] val (name, tags) = toMetricName(names)

    override def add(value: Float): Unit = client.recordHistogramValue(name, value, tags: _*)
  }

  private class DataDogGauge(names: Seq[String], f: () => Float) extends Gauge {
    val (name, tags) = toMetricName(names)
    gauges += names -> this

    override def remove(): Unit = gauges -= names

    def apply(): Float = f()
  }

  override def repr: DataDogStatsReceiver = this

  override def counter(v: Verbosity, names: String*): Counter = {
    verbosity += names -> v
    if (includeMetric(names)) new DataDogCounter(names) else NoopCounter
  }

  override def stat(v: Verbosity, names: String*): Stat = {
    verbosity += names -> v
    if (includeMetric(names)) new DataDogStat(names) else NoopStat
  }

  override def addGauge(v: Verbosity, names: String*)(f: => Float): Gauge = {
    verbosity += names -> v
    if (includeMetric(names)) new DataDogGauge(names, () => f) else NoopGauge
  }

  private def includeMetric(names: Seq[String]): Boolean = {
    names match {
      case "http" +: _ => false
      case "toggles" +: _ => false
      case "route" :: Method(_) +: _ => false
      case _ => true
    }
  }

  private def toMetricName(names: Seq[String]): (String, Seq[String]) =
    names match {
      case "clnt" :: clientName +: rest =>
        (sanitizeName("clnt" +: rest), Seq(s"client:${clientName.replace(':', '/')}"))
      case Seq("status", status) => ("http.requests", Seq(s"status:$status"))
      case Seq("time", status) => ("http.time", Seq(s"status:$status"))
      case Seq("response_size") => ("http.response_size", Seq.empty)
      case Seq("route", routeName, Method(method), metricName, Status(status)) =>
        (sanitizeName(Seq("http", metricName)), Seq(s"route:$routeName", s"method:$method", s"status:$status"))
      case Seq("route", routeName, Method(method), metricName) =>
        (sanitizeName(Seq("http", metricName)), Seq(s"route:$routeName", s"method:$method"))
      case _ => (sanitizeName(names), Seq.empty)
    }

  private def sanitizeName(name: Seq[String]) = name.map(_.replaceAll("[^\\w]", "_")).mkString(".")

  private object Method {
    def unapply(s: String): Option[String] = if (s.toUpperCase == s) Some(s) else None
  }

  private object Status {
    private[this] val chars = "1234567890X".toSet

    def unapply(s: String): Option[String] = if (s.forall(chars.contains)) Some(s) else None
  }

}
