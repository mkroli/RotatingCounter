package de.krolikowski.counter

import de.krolikowski.counter.impl.{SimpleRotatingCounter, StackedRotatingCounter}

trait RotatingCounter {
  var onExpiry: (Long) => Unit = (count: Long) => {}

  def add(count: Long): Unit

  def addOne = add(1L)

  def sum: Long

  def reset

  def +=(count: Long) = add(count)

  def apply() = sum

  def partitions: Seq[Long]
}

object RotatingCounter {
  def apply(size: Int, period: Long*): RotatingCounter =
    (for(p <- period) yield new SimpleRotatingCounter(p, size).asInstanceOf[RotatingCounter]) reduceLeft
      { new StackedRotatingCounter(_, _) }

  def apply(periodDetails: (Long, Int)*): RotatingCounter =
    periodDetails map
      { (p: (Long, Int)) => new SimpleRotatingCounter(p._1, p._2).asInstanceOf[RotatingCounter] } reduceLeft
        { new StackedRotatingCounter(_, _) }
}
