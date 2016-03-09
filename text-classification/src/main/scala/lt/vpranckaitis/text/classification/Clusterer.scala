package lt.vpranckaitis.text.classification

import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.KMeansMacQueen
import de.lmu.ifi.dbs.elki.algorithm.clustering.kmeans.initialization.RandomlyChosenInitialMeans
import de.lmu.ifi.dbs.elki.data.SparseDoubleVector
import de.lmu.ifi.dbs.elki.data.`type`.VectorFieldTypeInformation
import de.lmu.ifi.dbs.elki.database.StaticArrayDatabase
import de.lmu.ifi.dbs.elki.database.ids.{DBIDIter, DBIDRef}
import de.lmu.ifi.dbs.elki.datasource.MultipleObjectsBundleDatabaseConnection
import de.lmu.ifi.dbs.elki.datasource.bundle.MultipleObjectsBundle
import de.lmu.ifi.dbs.elki.math.linearalgebra.Vector
import de.lmu.ifi.dbs.elki.math.random.RandomFactory

import scala.collection.JavaConversions._

object Clusterer {
  case class Cluster(documents: Seq[Document], mean: Vector)
}

class Clusterer(dimensionality: Int, documents: Seq[Document]) {
  import Clusterer._

  val clusterer = new KMeansMacQueen(SparseCosineDistanceFunction.STATIC, 5, 10000, new RandomlyChosenInitialMeans(RandomFactory.DEFAULT))

  val typeInformation = new VectorFieldTypeInformation(SparseDoubleVector.FACTORY, dimensionality)
  val multipleObjectsBundle = MultipleObjectsBundle.makeSimple(typeInformation, documents.toList)
  val databaseConnection = new MultipleObjectsBundleDatabaseConnection(multipleObjectsBundle)
  val database = new StaticArrayDatabase(databaseConnection, null)
  database.initialize()

  def run() = {
    val clustering = clusterer.run(database)
    val clusters: Seq[Cluster] = clustering.getToplevelClusters map { cluster =>
      def getDocument(it: DBIDRef) = database.getBundle(it).data(1).asInstanceOf[Document]
      def iterate(it: DBIDIter): Stream[Document] = if (it.valid) getDocument(it) #:: iterate(it.advance()) else Stream.empty

      val documents = iterate(cluster.getIDs.iter()).toSeq
      val mean = cluster.getModel.getMean
      Cluster(documents, mean)
    }

    clusters
  }
}
