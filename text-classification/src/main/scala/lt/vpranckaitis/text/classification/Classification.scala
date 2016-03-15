package lt.vpranckaitis.text.classification

import de.lmu.ifi.dbs.elki.data.{NumberVector, SparseDoubleVector}
import lt.vpranckaitis.text.mining.Parsers
import lt.vpranckaitis.text.mining.entities.Movie
import spray.json._
import scala.io.Source
import ExtendedJsonProtocol._

object Classification extends App {
  val dataSet = Source.fromFile("movies.json").mkString.parseJson.convertTo[List[Movie]]
  val classificationPoints =
    Source.fromFile("classification-points.json").mkString.parseJson.convertTo[List[ClassificationPoint]]

  val movieTokens = dataSet map { m =>
    val tokens = Parsers.textToTokens(m.description)
    Common.processTokens(tokens)
  }

  val cpWords = classificationPoints flatMap { _.words.keys }
  val wordToId = (movieTokens.flatten.toSeq ++ cpWords).distinct.zipWithIndex.toMap

  val dimensionality = wordToId.size

  val documents = Common.tokensToDocuments(dataSet zip movieTokens, wordToId)

  val cpVectors = classificationPoints map { cp =>
    val (indexes, values) = (cp.words.toSeq map { case (key, value) => (wordToId(key), value) } sortBy { _._1 }).unzip
    new SparseDoubleVector(indexes.toArray, values.toArray, dimensionality)
  }

  def distance(a: NumberVector, b: NumberVector) = SparseCosineDistanceFunction.STATIC.distance(a, b)
  val groupedByPoints = documents groupBy { d => cpVectors minBy { distance(_, d) } }

  val cpVectorToClassificationPoint = (cpVectors zip classificationPoints).toMap

  val newClassificationPoints = groupedByPoints map { case (cpVector, docs) =>
    val matches = docs map { d => (distance(cpVector, d), d.movie.description) } sortBy { _._1 }
    val classificationPoint = cpVectorToClassificationPoint(cpVector)
    classificationPoint.copy(bestMatches = matches)
  }

  val matchesCount = 10

  def bestMatchestToText(bm: Seq[(Double, String)]) = bm map { case (score, text) => s"    $score, $text" }
  def makeListing(cp: ClassificationPoint) = {
    val header = s"  ${cp.category}"
    val matches = bestMatchestToText(cp.bestMatches take matchesCount)
    val others = s"    ...${cp.bestMatches.size - matchesCount} other"

    if (cp.bestMatches.size > matchesCount)
      (header +: matches :+ others) mkString "\n"
    else
      (header +: matches) mkString "\n"
  }

  println("Categories: ")
  for {
    cp <- newClassificationPoints
    listing = makeListing(cp)
  } println(listing)
}
