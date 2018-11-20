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

package ucl.pdd.logging

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.classic.jul.LevelChangePropagator
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.{Level, LoggerContext}
import ch.qos.logback.core.ConsoleAppender
import com.twitter.app.App
import io.sentry.Sentry
import io.sentry.logback.SentryAppender
import org.slf4j.{Logger, LoggerFactory}

/**
 * Trait helping with configuring Logback support for Twitter applications. It also enables a
 * bridge to Sentry (iff the `SENTRY_DSN` environment variable is defined).
 */
trait LoggingConfigurator {
  this: App =>

  init {
    initSentry()
    initLogback()
  }

  private def initSentry(): Unit = {
    // We do not configure sentry if the DSN is not defined, to avoid showing a warning in that
    // case (we may want no to report to Sentry, e.g., during development). It also mean that we
    // enforce using an environment-based configuration of Sentry (no Java properties).
    if (sys.env.contains("SENTRY_DSN")) {
      // Used to differentiate between libraries and our own code.
      // https://docs.sentry.io/clients/java/config/#in-application-stack-frames
      sys.props("sentry.stacktrace.app.packages") = "ucl.pdd"

      // Configure the environment.
      sys.props("sentry.environment") = sys.env.getOrElse("ENVIRONMENT", "devel")

      // This will initialize Sentry by looking for the `SENTRY_DSN` environment variable.
      Sentry.init()
    }
  }

  private def initLogback(): Unit = {
    // This method programmatically configures Logback, instead of using a logback.xml file.
    // It sends the logs to the console and to Sentry, if configured.

    // We assume SLF4J is bound to logback in the current environment.
    // This is enforced by the dependency of this package on Logback.
    val ctx = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val rootLogger = ctx.getLogger(Logger.ROOT_LOGGER_NAME)

    ctx.reset()

    if (sys.env.contains("SENTRY_DSN")) {
      val warnThresholdFilter = new ThresholdFilter
      warnThresholdFilter.setLevel("WARN")
      warnThresholdFilter.setContext(ctx)
      warnThresholdFilter.start()

      val sentryAppender = new SentryAppender
      sentryAppender.addFilter(warnThresholdFilter)
      sentryAppender.setName("Sentry")
      sentryAppender.setContext(ctx)
      sentryAppender.start()
      rootLogger.addAppender(sentryAppender)
    }

    val patternEncoder = new PatternLayoutEncoder
    patternEncoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n")
    patternEncoder.setContext(ctx)
    patternEncoder.start()

    val consoleAppender = new ConsoleAppender[ILoggingEvent]
    consoleAppender.setName("Console")
    consoleAppender.setEncoder(patternEncoder)
    consoleAppender.setContext(ctx)
    consoleAppender.start()
    rootLogger.addAppender(consoleAppender)

    val levelChangePropagator = new LevelChangePropagator
    levelChangePropagator.setContext(ctx)
    levelChangePropagator.start()
    ctx.addListener(levelChangePropagator)

    rootLogger.setLevel(Level.INFO)
    rootLogger.info("Switched logging level to INFO")
  }
}
