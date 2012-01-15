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
 * limitations under the License.
 */

package de.krolikowski.counter.impl

import de.krolikowski.counter.RotatingCounter

/**
 * An implementation of [[de.krolikowski.counter.RotatingCounter]] which can
 * be used to stack other implementations of
 * [[de.krolikowski.counter.RotatingCounter]] together. You need at least two
 * other [[de.krolikowski.counter.RotatingCounter]]S but you can have more.
 * @param rc1 the [[de.krolikowski.counter.RotatingCounter]] which should
 * store the oldest events
 * @param rc2 the [[de.krolikowski.counter.RotatingCounter]] which should
 * store the second oldest events
 * @param rcN more (if any) [[de.krolikowski.counter.RotatingCounter]]S which
 * should be sorted by the date of the events they should store (the most
 * current [[de.krolikowski.counter.RotatingCounter]] should be the last
 * element)
 */
class StackedRotatingCounter(
  rc1: RotatingCounter,
  rc2: RotatingCounter,
  rcN: RotatingCounter*) extends RotatingCounter {
  val pastRotatingCounter = rc1
  val currentRotatingCounter =
    if (rcN.isEmpty)
      rc2
    else
      new StackedRotatingCounter(rc2, rcN.head, rcN.drop(1): _*)
  pastRotatingCounter.onExpiry = (count: Long) => this.onExpiry(count)
  currentRotatingCounter.onExpiry = (count: Long) => pastRotatingCounter add count

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
