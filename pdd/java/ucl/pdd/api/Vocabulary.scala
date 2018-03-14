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
 * A vocabulary is a list of search queries that are tracked. A vocabulary is append-only, queries
 * cannot be removed once they have been added to enforce traceability.
 *
 * @param queries List of search queries tracked by this vocabulary.
 */
case class Vocabulary(queries: Seq[VocabularyQuery] = Seq.empty)

/**
 * A query of interest inside a vocabulary. Exactly one of the fields should be set.
 *
 * @param exact An exact query represent an entire search query.
 * @param terms Terms represent a list of tokens that must be simultaneously present in a search
 *              query. The order does not matter (as opposed to n-grams).
 */
case class VocabularyQuery(exact: Option[String] = None, terms: Option[Seq[String]] = None)
