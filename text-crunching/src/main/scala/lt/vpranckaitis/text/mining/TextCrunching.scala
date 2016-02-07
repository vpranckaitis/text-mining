package lt.vpranckaitis.text.mining

import scala.io.Source

object TextCrunching extends App {
  val moviesFile = Source.fromFile("plot.list").getLines.toStream

  val withoutHeader = moviesFile dropWhile { !_.matches("^MV:.*") }

  println(withoutHeader take 10 mkString "\n")
}
