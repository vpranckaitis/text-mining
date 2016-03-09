package lt.vpranckaitis.text.classification

import de.lmu.ifi.dbs.elki.data.SparseDoubleVector
import lt.vpranckaitis.text.mining.entities.Movie

class Document(indexes: Seq[Int], values: Seq[Double], dimensionality: Int, val movie: Movie)
  extends SparseDoubleVector(indexes.toArray, values.toArray, dimensionality)