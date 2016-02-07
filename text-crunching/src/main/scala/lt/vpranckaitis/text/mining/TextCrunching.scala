package lt.vpranckaitis.text.mining

import lt.vpranckaitis.text.mining.Helpers._

import scala.io.Source

object TextCrunching extends App {

  val moviesFile = Source.fromFile("plot.list", "ISO-8859-1").getLines

  val withoutHeader = moviesFile dropWhile { !_.matches("^MV:.*") }

  val havingEnoughCharacters = Parsers.textFileToMoviePlots(withoutHeader) filter { x => x.characters.size >= 2 } take 5000

  val characterCounts = (havingEnoughCharacters flatMap { _.characters }).foldLeft(Map.empty[String, Int] withDefaultValue 0) { (acc, a) => acc + (a -> (acc(a) + 1)) }

  println(characterCounts.toSeq.sortBy(_._2)(Ordering[Int].reverse) take 10 mkString "\n")
}
