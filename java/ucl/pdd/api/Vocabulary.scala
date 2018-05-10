package ucl.pdd.api

/**
 * A vocabulary is a list of search queries that are tracked. A vocabulary is append-only, queries
 * cannot be removed once they have been added to enforce traceability.
 *
 * @param queries List of search queries tracked by this vocabulary.
 * @param offset  In case this object represent a partial view over a vocabulary, indicates how many
 *                items where skipped. Useful when sending delta updates.
 */
case class Vocabulary(queries: Seq[VocabularyQuery] = Seq.empty, offset: Int = 0) {
  def drop(n: Int): Vocabulary = Vocabulary(queries = queries.drop(n), offset = n)
}

/**
 * A query of interest inside a vocabulary. Exactly one of the fields should be set.
 *
 * @param exact An exact query represent an entire search query.
 * @param terms Terms represent a list of tokens that must be simultaneously present in a search
 *              query. The order does not matter (as opposed to n-grams).
 */
case class VocabularyQuery(exact: Option[String] = None, terms: Option[Seq[String]] = None)
