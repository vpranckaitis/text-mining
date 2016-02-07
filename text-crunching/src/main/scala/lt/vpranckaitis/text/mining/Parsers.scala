package lt.vpranckaitis.text.mining

import lt.vpranckaitis.text.mining.entities.MoviePlot
import opennlp.tools.namefind.{NameFinderME, TokenNameFinderModel}
import opennlp.tools.sentdetect.{SentenceDetector, SentenceDetectorME, SentenceModel}
import opennlp.tools.tokenize.{Tokenizer, TokenizerME, TokenizerModel}

object Parsers {
  private lazy val nerModel = new TokenNameFinderModel(getClass.getResourceAsStream("/en-ner-person.bin"))
  private lazy val sentModel = new SentenceModel(getClass.getResourceAsStream("/en-sent.bin"));
  private lazy val tokenModel = new TokenizerModel(getClass.getResourceAsStream("/en-token.bin"));

  def textFileToMoviePlots(lines: Iterator[String]): Stream[MoviePlot] = {
    val (before, after) = lines span { !_.matches("^[-]+$") }
    val groupedByPrefix = before.toList filter { _.length > 3 } takeWhile { !_.startsWith("BY:") } groupBy { _ take 3 }
    val name = groupedByPrefix("MV:").head stripPrefix "MV: "
    val plot = groupedByPrefix("PL:") map { _ stripPrefix "PL: " } mkString " "
    MoviePlot(name, plot) #:: textFileToMoviePlots(after drop 1)
  }

  def textToCharacters(text: String) = {
    val sentenceDetector: SentenceDetector = new SentenceDetectorME(sentModel)
    val tokenizer: Tokenizer = new TokenizerME(tokenModel)
    val nameFinder = new NameFinderME(nerModel)
    val spans = for {
      sentence <- sentenceDetector.sentDetect(text)
      tokens = tokenizer.tokenize(sentence)
      spans = nameFinder.find(tokens)
      strings = spans map { s => tokens.slice(s.getStart, s.getEnd) mkString " " }
    } yield strings

    spans.flatten.distinct.toSeq
  }
}
