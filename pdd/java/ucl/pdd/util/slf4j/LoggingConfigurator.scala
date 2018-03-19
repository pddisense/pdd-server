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

package ucl.pdd.util.slf4j

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.twitter.app.App
import io.sentry.Sentry
import org.slf4j.LoggerFactory

trait LoggingConfigurator {
  this: App =>

  init {
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
