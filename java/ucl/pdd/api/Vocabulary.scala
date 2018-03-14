/*
 * Private Data Donor is a platform to collect search logs via crowd-sourcing.
 * Copyright (C) 2017-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * Private Data Donor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Private Data Donor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Private Data Donor.  If not, see <http://www.gnu.org/licenses/>.
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
