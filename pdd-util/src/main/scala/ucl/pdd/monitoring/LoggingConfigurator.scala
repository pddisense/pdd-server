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

import ch.qos.logback.classic.{Level, LoggerContext}
import com.twitter.app.App
import org.slf4j.{Logger, LoggerFactory}

/**
 * Trait helping with configuring logging support for Twitter applications.
 *
 * First, it configures Logback, and allows to change the default logging level
 * via a flag. Second, it configures Sentry (iff the `SENTRY_DSN` environment
 * variable is defined), which is integrated into Logback.
 */
trait LoggingConfigurator {
  this: App =>

  val logLevelFlag = flag(
    "log_level",
    "INFO",
    "Default root logging level. Values values are: 'ALL', 'TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR', 'OFF'.")

  // Sentry configuration. This is done in the constructor and not an `init` block because
  // Sentry is integrated with Logback and the latter is initialised very soon.
  {
    // Used to differentiate between libraries and our own code.
    // https://docs.sentry.io/clients/java/config/#in-application-stack-frames
    sys.props("sentry.stacktrace.app.packages") = "ucl.pdd"

    // Set the environment. The same environment variable is used to configure DataDog's
    // environment as well.
    sys.props("sentry.environment") = sys.env.getOrElse("ENVIRONMENT", "devel")
  }

  premain {
    // Programmatically set the root logging level. We cannot do it in an `init` block
    // because the flags are not parsed yet.
    val ctx = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
    val rootLogger = ctx.getLogger(Logger.ROOT_LOGGER_NAME)
    val level = Level.toLevel(logLevelFlag())
    if (level != Level.INFO) {
      // INFO is the default logging level as specified in the logback.xml file.
      rootLogger.setLevel(level)
      rootLogger.info(s"Switched logging level to $level")
    }
  }
}
