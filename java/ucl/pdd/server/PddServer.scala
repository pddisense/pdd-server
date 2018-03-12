package ucl.pdd.server

import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.inject.modules.StatsReceiverModule
import io.sentry.Sentry
import ucl.pdd.config.ConfigModule
import ucl.pdd.cron.CronModule
import ucl.pdd.storage.install.StorageModule

object PddServerMain extends PddServer

class PddServer extends HttpServer with LogbackConfigurator {
  override def modules = Seq(ConfigModule, StorageModule, CronModule, StatsReceiverModule)

  override def jacksonModule = PddJacksonModule

  premain {
    // Used to differentiate between libraries and our own code.
    // https://docs.sentry.io/clients/java/config/#in-application-stack-frames
    sys.props("sentry.stacktrace.app.packages") = "ucl"

    // This will initialize Sentry by looking for the `SENTRY_DSN` environment variable or the
    // `sentry.dsn` system property.
    Sentry.init()
  }

  override def configureHttp(router: HttpRouter): Unit = {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .add[CampaignsController]
      .add[ClientsController]
      .add[SketchController]
  }
}
