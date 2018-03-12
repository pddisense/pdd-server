package ucl.pdd.config

import java.util.concurrent.TimeUnit

import com.twitter.inject.TwitterModule
import com.twitter.util.{Duration => TwitterDuration}
import org.joda.time.{DateTimeZone, Duration}

object ConfigModule extends TwitterModule {
  private[this] val dayDurationFlag = flag("day_duration", TwitterDuration.fromTimeUnit(1, TimeUnit.DAYS), "Duration of one day")
  private[this] val timezoneFlag = flag("timezone", "Europe/London", "Reference timezone")

  override def configure(): Unit = {
    bind[Duration].annotatedWith[DayDuration].toInstance(new Duration(dayDurationFlag().inMillis))
    bind[DateTimeZone].annotatedWith[Timezone].toInstance(DateTimeZone.forID(timezoneFlag()))
  }
}
