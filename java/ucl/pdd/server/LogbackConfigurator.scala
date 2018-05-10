/*
 * Accio is a platform to enforce experiments reproducibility.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Accio is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Accio is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Accio.  If not, see <http://www.gnu.org/licenses/>.
 */

package ucl.pdd.server

import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.{Level, Logger, LoggerContext}
import ch.qos.logback.core.joran.spi.JoranException
import ch.qos.logback.core.util.StatusPrinter
import com.twitter.inject.Logging
import org.slf4j.LoggerFactory

/**
 * Traits to force the initialisation of Logback's configuration, following Bazel/Pants conventions. It should be
 * included only once per application, typically in the class implementing the `main()` method.
 *
 * By default, logback.xml is only loaded if present at the root of resources path. However, because Bazel/Pants puts
 * resources in namespaces. This trait loads a logback.xml file located under the same package than this class
 * (i.e., the concrete class implementing this trait).
 */
trait LogbackConfigurator {
  // All of the following code is indeed executed as part of the constructor. It is scoped to prevent polluting the
  // object attributes.
  {
    val logbackPath = getClass.getPackage.getName.replace(".", "/") + "/logback.xml"
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
      LoggerFactory.getLogger(classOf[LogbackConfigurator])
        .debug(s"Loaded logback configuration from resource $logbackPath")
    }
  }
}

object LogbackConfigurator extends Logging {
  def setLevel(level: Level): Unit = {
    LoggerFactory.getLogger("ucl").asInstanceOf[Logger].setLevel(level)
    logger.info(s"Set logging level: $level")
  }
}
