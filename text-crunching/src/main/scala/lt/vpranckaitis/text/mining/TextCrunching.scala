package lt.vpranckaitis.text.mining

import opennlp.tools.stemmer.PorterStemmer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.io.Source

object TextCrunching extends App {
  val count = 5000

  val moviesFile = Source.fromFile("plot.list", "ISO-8859-1").getLines

  val withoutHeader = moviesFile dropWhile { !_.matches("^MV:.*") }

  val time = System.currentTimeMillis()

  val plots = Parsers.textFileToMovies(withoutHeader) take count map Parsers.movieToPlot

  def top10(x: Seq[(String, Int)]): Seq[(String, Int)] = x.sortBy(_._2)(Ordering[Int].reverse) take 10
  def top10(x: Map[String, Int]): Seq[(String, Int)] = top10(x.toSeq)

  val locations = plots flatMap { _.locations } groupBy { l => l } mapValues { _.size }
  val top10Locations = top10(locations)

  val characters = plots flatMap { _.characters } groupBy { l => l } mapValues { _.size }
  val top10Characters = top10(characters)

  val plotsWithTop10Characters = plots filter { p => p.characters exists { c => top10Characters exists { _._1 == c } } }

  val tripletFutures = plotsWithTop10Characters map { x => Future(Parsers.sentencesToTriplets(x.sentences)) }

  val x = Future.sequence(tripletFutures.toList) // map { _ foreach { _ foreach println } }
  val triplets = Await.result(x, Duration.Inf).flatten

  val stem: (String => String) = new PorterStemmer().stem
  val verbBaseForm: (String => String) = new MorphologyAnalyzer("./text-crunching/dict").getVerbBaseForm

  val groupedByCharacter = triplets filter { t => top10Characters exists { _._1 == t.subject } } groupBy { _.subject } mapValues { ts =>
    val (tsPersonObject, tsNonPersonObject) = ts partition { t => characters contains t.`object` }
    val top10RelatedPeople = top10(tsPersonObject groupBy { _.`object` } mapValues { _.size })
    val top10Relations = top10(tsPersonObject groupBy { t => verbBaseForm(t.predicate) } mapValues { _.size })
    val top10Actions = top10(tsNonPersonObject groupBy { t => verbBaseForm(t.predicate) } mapValues { _.size })
    (top10RelatedPeople, top10Relations, top10Actions)
  } withDefaultValue (Seq.empty, Seq.empty, Seq.empty)

  def makeListing(indent: Int, subIndent: Int, description: String, list: Seq[(String, Int)]): String = {
    val spaces = "                          "
    val header = s"${spaces take indent}${list.size} $description"
    val listings = list map { case (x, n) => s"${spaces take subIndent}$x, $n" }
    (header +: listings) mkString "\n"
  }

  val locationListing = makeListing(0, 2, "most common locations", top10Locations)
  val characterListings = for {
    (character, times) <- top10Characters
    characterLine = s"  $character, $times"
    (people, relations, actions) = groupedByCharacter(character)
    peopleListing = makeListing(4, 6, "most related people", people)
    relationsListing = makeListing(4, 6, "most common relations", relations)
    actionsListing = makeListing(4, 6, "most common actions", actions)
  } yield Seq(characterLine, peopleListing, relationsListing, actionsListing).mkString("\n")

  val listing = (locationListing +: s"${characterListings.size} most common people:" +: characterListings) mkString "\n"

  println(listing)

  println((System.currentTimeMillis() - time) * 0.001)
}
