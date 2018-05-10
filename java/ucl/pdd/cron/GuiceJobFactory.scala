package ucl.pdd.cron

import com.google.inject.{Inject, Injector}
import org.quartz.{Job, Scheduler}
import org.quartz.spi.{JobFactory, TriggerFiredBundle}

private[cron] final class GuiceJobFactory @Inject()(injector: Injector) extends JobFactory {
  override def newJob(bundle: TriggerFiredBundle, scheduler: Scheduler): Job = {
    injector.getInstance(bundle.getJobDetail.getJobClass)
  }
}
