/*
 * Copyright 2012 Michael Krolikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *limitations under the License.
 */

package de.krolikowski.counter.impl

import de.krolikowski.counter.RotatingCounter

class SimpleRotatingCounter(period: Long, size: Int) extends RotatingCounter {
  private val counterPartitions = new Array[Long](size)
  private var lastAccessTime: Long = _
  private var lastAccessIndex: Int = _

  private def partitionOfTime(time: Long) = (time * size) / period

  private def rotatedPartitionOfTime(now: Long) = partitionOfTime(now % period).asInstanceOf[Int]

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
      if (partitionOfTime(lastAccessTime + period) <= partitionOfTime(now)) {
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
    val index = rotatedPartitionOfTime(now)
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
      expire(now, rotatedPartitionOfTime(now))
      counterPartitions reduce { _ + _ }
    }
  }

  override def reset: Unit = counterPartitions synchronized resetRotating(0, size - 1)

  override def partitions = {
    val now = System.currentTimeMillis
    val index = rotatedPartitionOfTime(now)
    counterPartitions synchronized {
      expire(now, index)
      (counterPartitions drop (index + 1)) ++ (counterPartitions take (index + 1))
    }
  }
}
