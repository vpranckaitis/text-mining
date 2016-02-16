package lt.vpranckaitis.text.mining

import lt.vpranckaitis.text.mining.entities.{Movie, Plot}
import opennlp.tools.namefind.{NameFinderME, TokenNameFinderModel}
import opennlp.tools.parser.ParserModel
import opennlp.tools.sentdetect.{SentenceDetector, SentenceDetectorME, SentenceModel}
import opennlp.tools.tokenize.{Tokenizer, TokenizerME, TokenizerModel}
import opennlp.tools.util.Span

object Parsers {
  private lazy val personModel = new TokenNameFinderModel(getClass.getResourceAsStream("/en-ner-person.bin"))
  private lazy val locationModel = new TokenNameFinderModel(getClass.getResourceAsStream("/en-ner-location.bin"))
  private lazy val sentenceModel = new SentenceModel(getClass.getResourceAsStream("/en-sent.bin"))
  private lazy val tokenModel = new TokenizerModel(getClass.getResourceAsStream("/en-token.bin"))
  private lazy val parserModel = new ParserModel(getClass.getResourceAsStream("/en-token.bin"))

  def textFileToMovies(lines: Iterator[String]): Stream[Movie] = {
    val (before, after) = lines span { !_.matches("^[-]+$") }
    val groupedByPrefix = before.toList filter { _.length > 3 } takeWhile { !_.startsWith("BY:") } groupBy { _ take 3 }
    val name = groupedByPrefix("MV:").head stripPrefix "MV: "
    val plot = groupedByPrefix("PL:") map { _ stripPrefix "PL: " } mkString " "
    Movie(name, plot) #:: textFileToMovies(after drop 1)
  }

  def textToMovieCharacters(text: String) = {
    val sentenceDetector: SentenceDetector = new SentenceDetectorME(sentenceModel)
    val tokenizer: Tokenizer = new TokenizerME(tokenModel)

    val nameFinder = new NameFinderME(personModel)
    val spans = for {
      sentence <- sentenceDetector.sentDetect(text)
      tokens = tokenizer.tokenize(sentence)
      spans = nameFinder.find(tokens)
      strings = spans map { s => tokens.slice(s.getStart, s.getEnd) mkString " " }
    } yield strings

    spans.flatten.distinct.toSeq
  }

  def movieToPlot(movie: Movie) = {

    def deSpan(sentence: Seq[String])(s: Span) = sentence.slice(s.getStart, s.getEnd) mkString " "

    val sentenceDetector = new SentenceDetectorME(sentenceModel).sentDetect _
    val tokenize = new TokenizerME(tokenModel).tokenize _
    val findLocationSpans: (Array[String] => Array[Span]) = new NameFinderME(locationModel).find _
    val findPeopleSpans: (Array[String] => Array[Span]) = new NameFinderME(personModel).find _

    val sentences = sentenceDetector(movie.description) map tokenize

    val locations = for {
      sentence <- sentences
      span <- findLocationSpans(sentence)
    } yield deSpan(sentence)(span)

    val characters = for {
      sentence <- sentences
      span <- findPeopleSpans(sentence)
    } yield deSpan(sentence)(span)

    Plot(movie.description, locations.distinct, characters.distinct)
  }

  def descriptionToTriplets(description: String) = {
    val sentenceDetector = new SentenceDetectorME(sentenceModel).sentDetect _
    val tokenize = new TokenizerME(tokenModel).tokenize _
  }
}
