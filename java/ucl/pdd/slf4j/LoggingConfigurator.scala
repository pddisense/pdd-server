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

package ucl.pdd.slf4j

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.twitter.app.App
import io.sentry.Sentry
import org.slf4j.LoggerFactory

trait LoggingConfigurator {
  this: App =>

  premain {
    initSentry()
    initLogback()
  }

  private def initSentry(): Unit = {
    // Used to differentiate between libraries and our own code.
    // https://docs.sentry.io/clients/java/config/#in-application-stack-frames
    sys.props("sentry.stacktrace.app.packages") = "ucl"

    // This will initialize Sentry by looking for the `SENTRY_DSN` environment variable or the
    // `sentry.dsn` system property.
    Sentry.init()
  }

  private def initLogback(): Unit = {
    val logbackPath = classOf[LoggingConfigurator].getPackage.getName.replace(".", "/") + "/logback.xml"
    val is = getClass.getClassLoader.getResourceAsStream(logbackPath)
    if (null != is) {
      // We assume SLF4J is bound to logback in the current environment.
      val ctx = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
      try {
        val configurator = new JoranConfigurator
        configurator.setContext(ctx)
        // Call context.reset() to clear any previous configuration, e.g. default
        // configuration. For multi-step configuration, omit calling context.reset().
        ctx.reset()
        configurator.doConfigure(is)
      } catch {
        case _: JoranException => // StatusPrinter will handle this.
      }
      StatusPrinter.printInCaseOfErrorsOrWarnings(ctx)
      LoggerFactory
        .getLogger(classOf[LoggingConfigurator])
        .debug(s"Loaded logback configuration from resource $logbackPath")
    }
  }
}
