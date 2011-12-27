package de.krolikowski.counter.impl

import de.krolikowski.counter.RotatingCounter

class SimpleRotatingCounter(period: Long, size: Int) extends RotatingCounter {
  private val _partitions = new Array[Long](size)
  private var lastAccessTime: Long = _
  private var lastAccessIndex: Int = _

  private def currentPartition(now: Long) =
    (((now % period) * size) / period).asInstanceOf[Int]

  @scala.annotation.tailrec
  private def resetRotating(start: Int, end: Int, expired: Long = 0L): Long = {
    val count = expired + _partitions(start)
    _partitions(start) = 0L
    if (start == end)
      count
    else
      resetRotating((start + 1) % size, end, count)
  }

  private def expire(now: Long, index: Int) {
    var count: Long = 0L
    _partitions synchronized {
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
    _partitions synchronized {
      expire(now, index)
      lastAccessTime = now
      lastAccessIndex = index
      _partitions(index) += count
    }
  }

  override def sum: Long = {
    val now = System.currentTimeMillis
    _partitions synchronized {
      expire(now, currentPartition(now))
      _partitions reduce { _ + _ }
    }
  }

  override def reset: Unit = resetRotating(0, size - 1)

  override def partitions = _partitions.clone()
}
