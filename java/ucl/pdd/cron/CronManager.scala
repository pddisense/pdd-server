package ucl.pdd.cron

import com.github.nscala_time.time.Imports._
import com.google.common.util.concurrent.AbstractIdleService
import com.google.inject.{Inject, Singleton}
import org.quartz._
import ucl.pdd.config.{DayDuration, Timezone}

@Singleton
final class CronManager @Inject()(
  scheduler: Scheduler,
  @DayDuration dayDuration: Duration,
  @Timezone timezone: DateTimeZone)
  extends AbstractIdleService {

  override protected def startUp(): Unit = {
    scheduler.start()
    scheduleDaily(scheduler, classOf[AggregateSketchesJob], 0)
    scheduleDaily(scheduler, classOf[CreateSketchesJob], 30)
  }

  override protected def shutDown(): Unit = {
    scheduler.shutdown()
  }

  private def scheduleDaily(scheduler: Scheduler, jobClass: Class[_ <: Job], minute: Int): Unit = {
    val jobDetail = JobBuilder
      .newJob(jobClass)
      .withIdentity(jobClass.getSimpleName, "PddJob")
      .build
    val trigger = TriggerBuilder
      .newTrigger
      .withIdentity(jobClass.getSimpleName + "Trigger")
      .startAt(DateBuilder.tomorrowAt(0, minute, 0))
      .withSchedule(
        SimpleScheduleBuilder
          .simpleSchedule
          .withIntervalInMilliseconds(dayDuration.millis)
          .repeatForever())
      .build
    scheduler.scheduleJob(jobDetail, trigger)
  }
}
