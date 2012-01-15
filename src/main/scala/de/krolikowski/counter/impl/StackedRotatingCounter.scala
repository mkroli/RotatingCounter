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

class StackedRotatingCounter(rotatingCounters: RotatingCounter*) extends RotatingCounter {
  val pastRotatingCounter = rotatingCounters.head
  val currentRotatingCounter = if (rotatingCounters.length == 2) {
    rotatingCounters.last
  } else {
    new StackedRotatingCounter(rotatingCounters.drop(1): _*)
  }
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
