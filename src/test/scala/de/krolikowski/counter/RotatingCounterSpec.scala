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

import org.scalatest.Spec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import de.krolikowski.counter.impl.SimpleRotatingCounter
import de.krolikowski.counter.impl.StackedRotatingCounter

@RunWith(classOf[JUnitRunner])
class RotatingCounterSpec extends Spec {
  describe("A RotatingCounter") {
    it("should calculate the sum of events over a period of time") {
      val counter = new SimpleRotatingCounter(1000L, 10) with Shortcuts
      for (i <- 1 to 10) {
        counter += 1
        Thread.sleep(10)
      }
      assert(counter() == 10)
    }

    it("should clear all partitions if last event is older than the counter's period") {
      val counter = new SimpleRotatingCounter(100L, 10) with Shortcuts
      counter += 1
      Thread.sleep(80)
      assert(counter() == 1)
      Thread.sleep(30)
      assert(counter() == 0)
    }

    it("should clear some partitions if last event is older than period/partitions") {
      val counter = new SimpleRotatingCounter(100L, 10) with Shortcuts
      counter += 1
      Thread.sleep(20)
      counter += 2
      assert(counter() == 3)
      Thread.sleep(80)
      assert(counter() == 2)
      Thread.sleep(30)
      assert(counter() == 0)
    }

    it("should return the partitions starting from the oldest partition") {
      val counter = new StackedRotatingCounter(
        RotatingCounter(80L, 2),
        RotatingCounter(80L, 2)) with Shortcuts
      counter.add

      assert(counter.partitions == List(0, 0, 0, 1))
      Thread.sleep(40)
      assert(counter.partitions == List(0, 0, 1, 0))
      Thread.sleep(40)
      assert(counter.partitions == List(0, 1, 0, 0))
      Thread.sleep(40)
      assert(counter.partitions == List(1, 0, 0, 0))
      Thread.sleep(40)
      assert(counter.partitions == List(0, 0, 0, 0))
    }

    it("should notify on limits being reached") {
      var limitReached = Array(false, false)
      val counter = new SimpleRotatingCounter(10000L, 100) with Shortcuts with Limits
      counter.limit(10) {
        limitReached(0) = true
      }
      counter.limit(15) {
        limitReached(1) = true
      }

      for (i <- 1 to 10) {
        assert(!limitReached(0) && !limitReached(1))
        counter.add
      }
      assert(limitReached(0) && !limitReached(1))
      for (i <- 1 to 5) {
        assert(limitReached(0) && !limitReached(1))
        counter.add
      }
      assert(limitReached(0) && limitReached(1))
    }
  }
}
