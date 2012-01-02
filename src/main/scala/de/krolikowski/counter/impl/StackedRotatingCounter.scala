package de.krolikowski.counter.impl

import de.krolikowski.counter.RotatingCounter

class StackedRotatingCounter(pastRotatingCounter: RotatingCounter, currentRotatingCounter: RotatingCounter) extends RotatingCounter {
  currentRotatingCounter.onExpiry = (count: Long) => pastRotatingCounter add count
  pastRotatingCounter.onExpiry = (count: Long) => this.onExpiry(count)

  override def add(count: Long) = currentRotatingCounter add count

  override def sum = this synchronized {
    pastRotatingCounter.sum + currentRotatingCounter.sum
  }

  override def reset {
    this synchronized {
      pastRotatingCounter.reset
      currentRotatingCounter.reset
    }
  }

  override def partitions = this synchronized {
    val currentPartitions = currentRotatingCounter.partitions
    val pastPartitions = pastRotatingCounter.partitions
    pastPartitions ++ currentPartitions
  }
}
