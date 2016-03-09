package lt.vpranckaitis.text.classification

import de.lmu.ifi.dbs.elki.data.{NumberVector, SparseNumberVector}
import de.lmu.ifi.dbs.elki.distance.distancefunction.CosineDistanceFunction

import scala.annotation.tailrec

object SparseCosineDistanceFunction {
  val STATIC = new SparseCosineDistanceFunction()
}

class SparseCosineDistanceFunction extends CosineDistanceFunction {
  def distance(v1: SparseNumberVector, v2: NumberVector): Double = {
    @tailrec
    def crossProduct(iter: Int, value: Double = 0): Double = {
      if (!v1.iterValid(iter)) value
      else {
        val dim = v1.iterDim(iter)
        val mult = v1.iterDoubleValue(iter) * v2.doubleValue(dim)
        crossProduct(v1.iterAdvance(iter), value + mult)
      }
    }

    @tailrec
    def squaredLength(iter: Int, value: Double = 0): Double = {
      if (!v1.iterValid(iter)) value
      else {
        val dim = v1.iterDim(iter)
        val mult = v1.iterDoubleValue(iter) * v1.iterDoubleValue(iter)
        squaredLength(v1.iterAdvance(iter), value + mult)
      }
    }

    @tailrec
    def squaredLength2(dim: Int = 0, value: Double = 0): Double = {
      if (dim == v2.getDimensionality) value
      else {
        val mult = v2.doubleValue(dim) * v2.doubleValue(dim)
        squaredLength2(dim + 1, value + mult)
      }
    }

    val cross = crossProduct(v1.iter)
    val l1 = squaredLength(v1.iter)
    val l2 = squaredLength2()

    if (cross == 0.0) 1.0
    else if (l1 == 0.0 || l2 == 0.0) 0.0
    else {
      val a = Math.sqrt((cross / l1) * (cross / l2))
      if ((a < 1.0)) 1 - a else 0.0
    }
  }
  override def distance(v1: NumberVector, v2: NumberVector): Double = {
    if (v1.isInstanceOf[SparseNumberVector])  distance(v1.asInstanceOf[SparseNumberVector], v2)
    else if (v2.isInstanceOf[SparseNumberVector]) distance(v2.asInstanceOf[SparseNumberVector], v1)
    else super.distance(v1, v2)
  }
}