package ucl.pdd.strategy

import ucl.pdd.api.Client

trait Strategy {
  def apply(clients: Seq[Client], attrs: StrategyAttrs): Seq[Seq[Client]]
}

case class StrategyAttrs(groupSize: Int)
