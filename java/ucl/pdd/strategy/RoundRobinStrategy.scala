package ucl.pdd.strategy

import ucl.pdd.api.Client

object RoundRobinStrategy extends Strategy {
  override def apply(clients: Seq[Client], attrs: StrategyAttrs): Seq[Seq[Client]] = {
    clients.grouped(attrs.groupSize).toSeq
  }
}
