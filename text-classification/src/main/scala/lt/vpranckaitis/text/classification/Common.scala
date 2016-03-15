package lt.vpranckaitis.text.classification

import lt.vpranckaitis.text.mining.entities.Movie
import opennlp.tools.stemmer.PorterStemmer

object Common {
  private val stemmer = new PorterStemmer()

  def processTokens(tokens: Seq[String]) =
    tokens withFilter { x => !(x matches "[\\d\\W]+") && x.length > 3 } map { x => stemmer.stem(x.toLowerCase) }

  def tokensToDocuments(moviesWithTokens: Seq[(Movie, Seq[String])], wordToId: Map[String, Int]) = {
    val dimensionality = wordToId.size
    moviesWithTokens map { case (movie, tokens) =>
      val bagOfWords = tokens groupBy { x => x } map { case (key, value) => (wordToId(key), value.size.toDouble) }
      val (indexes, values) = bagOfWords.toSeq.sortBy(_._1).unzip
      new Document(indexes, values, dimensionality, movie)
    }
  }

}
