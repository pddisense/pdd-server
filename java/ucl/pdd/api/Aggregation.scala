package ucl.pdd.api

/**
 * An aggregation is a request to collect searches during a given time period as part of a
 * campaign. Each campaign is divided in several aggregations, usually one per day.
 */
case class Aggregation(
  name: String,
  campaignName: String,
  day: Int,
  decryptedValues: Seq[Long],
  rawValues: Seq[Long],
  stats: AggregationStats)

case class AggregationStats(
  activeCount: Long,
  submittedCount: Long,
  decryptedCount: Long)
