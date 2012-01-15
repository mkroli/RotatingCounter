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

/**
 * With this trait you can add callback functions to an implementation of
 * [[de.krolikowski.counter.RotatingCounter]] which are called if some
 * limit is reached.
 * For example if you want a counter over the last minute and do some
 * notification if the counter is above 100 you could do it like this:
 * <pre>
 * val counter = new SimpleRotatingCounter(60000, 100) with Limits
 * counter.limit(100)
 *   println("Limit of 100 reached!")
 * }
 * </pre>
 */
trait Limits extends RotatingCounter {
  private var limits: List[(Long, () => Unit)] = List()

  def limit(limit: Long)(f: => Unit) {
    limits = (limit, () => f) :: limits
  }

  abstract override def add(count: Long) {
    val oldSum = sum
    super.add(count)
    for {
      (limit, callback) <- limits
      if (limit > oldSum && limit <= (oldSum + count))
    } callback()
  }
}
