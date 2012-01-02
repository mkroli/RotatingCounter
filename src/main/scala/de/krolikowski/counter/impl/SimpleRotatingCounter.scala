package de.krolikowski.counter.impl

import de.krolikowski.counter.RotatingCounter

class SimpleRotatingCounter(period: Long, size: Int) extends RotatingCounter {
  private val counterPartitions = new Array[Long](size)
  private var lastAccessTime: Long = _
  private var lastAccessIndex: Int = _

  private def currentPartition(now: Long) =
    (((now % period) * size) / period).asInstanceOf[Int]

  @scala.annotation.tailrec
  private def resetRotating(start: Int, end: Int, expired: Long = 0L): Long = {
    val count = expired + counterPartitions(start)
    counterPartitions(start) = 0L
    if (start == end)
      count
    else
      resetRotating((start + 1) % size, end, count)
  }

  private def expire(now: Long, index: Int) {
    var count: Long = 0L
    counterPartitions synchronized {
      if (lastAccessTime + period < now) {
        count = resetRotating(0, size - 1)
      } else if (lastAccessIndex != index) {
        count = resetRotating((lastAccessIndex + 1) % size, index)
      }
    }
    if (count > 0)
      onExpiry(count)
  }

  override def add(count: Long) {
    val now = System.currentTimeMillis
    val index = currentPartition(now)
    counterPartitions synchronized {
      expire(now, index)
      lastAccessTime = now
      lastAccessIndex = index
      counterPartitions(index) += count
    }
  }

  override def sum: Long = {
    val now = System.currentTimeMillis
    counterPartitions synchronized {
      expire(now, currentPartition(now))
      counterPartitions reduce { _ + _ }
    }
  }

  override def reset: Unit = counterPartitions synchronized resetRotating(0, size - 1)

  override def partitions = {
    val now = System.currentTimeMillis
    val index = currentPartition(now)
    counterPartitions synchronized {
      expire(now, currentPartition(now))
      (counterPartitions drop index + 1) ++ (counterPartitions take index + 1)
    }
  }
}
