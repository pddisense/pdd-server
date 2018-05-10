package ucl.pdd.storage

import com.google.common.util.concurrent.Service

trait Storage extends Service {
  def clients: ClientStore

  def campaigns: CampaignStore

  def aggregations: AggregationStore

  def sketches: SketchStore
}
