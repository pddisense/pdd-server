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

      // Configure the environment and tags.
      sys.props("sentry.environment") = sys.env.getOrElse("ENVIRONMENT", "devel")
      sys.props("sentry.tags") = s"role:${sys.env.getOrElse("ROLE", "pdd")}"

      // This will initialize Sentry by looking for the `SENTRY_DSN` environment variable.
      Sentry.init()
    }
  }

  private def initLogback(): Unit = {
    // We assume SLF4J is bound to logback in the current environment.
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
