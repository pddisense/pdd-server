package ucl.pdd.cron

import com.google.inject.{Provides, Singleton}
import com.twitter.inject.{Injector, TwitterModule}
import org.quartz._
import org.quartz.impl.StdSchedulerFactory
import org.quartz.spi.JobFactory

object CronModule extends TwitterModule {
  override def configure(): Unit = {
    bind[JobFactory].to[GuiceJobFactory]
  }

  @Provides
  @Singleton
  def providesScheduler(jobFactory: JobFactory): Scheduler = {
    val scheduler = StdSchedulerFactory.getDefaultScheduler
    scheduler.setJobFactory(jobFactory)
    scheduler
  }

  override def singletonStartup(injector: Injector): Unit = {
    injector.instance[CronManager].startAsync().awaitRunning()
  }

  override def singletonShutdown(injector: Injector): Unit = {
    injector.instance[CronManager].stopAsync().awaitTerminated()
  }
}
