package ucl.pdd.storage.memory

import com.google.common.util.concurrent.AbstractIdleService
import ucl.pdd.storage._

final class MemoryStorage extends AbstractIdleService with Storage {
  override val clients: ClientStore = new MemoryClientStore

  override val campaigns: CampaignStore = new MemoryCampaignStore

  override val aggregations: AggregationStore = new MemoryAggregationStore

  override val sketches: SketchStore = new MemorySketchStore

  override def shutDown(): Unit = {}

  override def startUp(): Unit = {}
}


