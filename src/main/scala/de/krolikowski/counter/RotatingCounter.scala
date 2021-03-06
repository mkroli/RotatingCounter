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

package de.krolikowski.counter

import de.krolikowski.counter.impl.{ SimpleRotatingCounter, StackedRotatingCounter }

/**
 * This is the base trait for implementations of RotatingCounter.
 */
trait RotatingCounter {
  /**
   * A listener which is called when events expire.
   * The listener doesn't need to be called at the moment the events would
   * expire but can be called at any later time (e.g. if someone adds or reads
   * events).
   */
  var onExpiry: (Long) => Unit = (count: Long) => {}

  /**
   * Should be called at the time the event(s) occur.
   *
   * @param count the number of occurred events
   */
  def add(count: Long): Unit

  /**
   * Calculates the current sum of all partitions.
   *
   * @return the number of registered events occurred in the given period of time
   */
  def sum(): Long

  /**
   * Resets the complete counter. This means all partitions are set to 0.
   */
  def reset()

  /**
   * Gives access to the inside of this
   * [[de.krolikowski.counter.RotatingCounter]].
   * @return a sequence of counters which represent the whole period of this
   * [[de.krolikowski.counter.RotatingCounter]] starting from the oldest
   * partition
   */
  def partitions(): Seq[Long]
}
