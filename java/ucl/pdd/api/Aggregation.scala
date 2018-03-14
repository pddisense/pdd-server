/*
 * Copyright 2017-2018 UCL / Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
