package lt.vpranckaitis.text.mining

import edu.smu.tspell.wordnet.impl.file.{Morphology, WordFormLookup}
import edu.smu.tspell.wordnet.{SynsetType, WordNetDatabase}

class MorphologyAnalyzer(wordnetDatabasePath: String) {
  System.setProperty("wordnet.database.dir", wordnetDatabasePath)

  private val database = WordNetDatabase.getFileInstance
  private val morphology = Morphology.getInstance

  private val wfl = WordFormLookup.getInstance

  def getVerbBaseForm(s: String) = {
    val synsets = wfl.getSynsets(s, Seq(SynsetType.VERB).toArray, true)
    val candidates = morphology.getBaseFormCandidates(s, SynsetType.VERB).toSet
    val counted =  synsets flatMap { _.getWordForms } groupBy { x => x } mapValues { _.size }
    val sorted = counted.toSeq.sortBy(_._2)(Ordering[Int].reverse) map { _._1 }
    val fromCandidates = sorted find { candidates contains _ }
    fromCandidates orElse sorted.headOption getOrElse s
  }
}
