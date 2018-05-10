package ucl.pdd.strategy

import ucl.pdd.api.Client

object NoStrategy extends Strategy {
  override def apply(clients: Seq[Client], attrs: StrategyAttrs): Seq[Seq[Client]] = {
    Seq(clients)
  }
}
