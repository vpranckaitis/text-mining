package lt.vpranckaitis.text.mining

import scala.io.Source

object TextCrunching extends App {

  val moviesFile = Source.fromFile("plot.list", "ISO-8859-1").getLines

  val withoutHeader = moviesFile dropWhile { !_.matches("^MV:.*") }

  println(Parsers.textFileToMoviePlots(withoutHeader) filter { x => Parsers.textToCharacters(x.plot).size >= 2 } take 100 mkString "\n")
}
