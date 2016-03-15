package lt.vpranckaitis.text.classification

import java.io.{File, PrintWriter}

import lt.vpranckaitis.text.classification.ExtendedJsonProtocol._
import lt.vpranckaitis.text.mining.Parsers
import lt.vpranckaitis.text.mining.entities.Movie
import opennlp.tools.stemmer.PorterStemmer
import spray.json._

import scala.io.Source

object Learning extends App {

  val movies = Source.fromFile("movies.json").mkString.parseJson.convertTo[List[Movie]]

  val dataSet = movies.grouped(5).toList map { _.head }

  val movieTokens = dataSet map { m =>
    val tokens = Parsers.textToTokens(m.description)
    Common.processTokens(tokens)
  }

  val wordToId = movieTokens.flatten.toSeq.distinct.zipWithIndex.toMap
  val idToWord = wordToId map { case (key, value) => (value, key) }

  val dimensionality = wordToId.size

  val documents = Common.tokensToDocuments(dataSet zip movieTokens, wordToId)

  val clusters = new Clusterer(dimensionality, documents.toSeq).run()

  val classificationPoints = clusters map { cluster =>
    def distanceFromMean(document: Document) = SparseCosineDistanceFunction.STATIC.distance(document, cluster.mean)
    val bestMatches = cluster.documents map { d => (distanceFromMean(d), d.movie.description) } sortBy { _._1 } take 5
    val words = cluster.mean.getArrayCopy.zipWithIndex withFilter { x => Math.abs(x._1) > 1e-6 } map { case (value, index) => (idToWord(index), value) }

    ClassificationPoint(words.toMap, "", bestMatches)
  }

  val writer = new PrintWriter(new File("classification-points.json"))
  writer.write(classificationPoints.toJson.prettyPrint)
  writer.close()
}
