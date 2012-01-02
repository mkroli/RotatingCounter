package de.krolikowski.counter

import org.scalatest.Spec
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

@RunWith(classOf[JUnitRunner])
class RotatingCounterSpec extends Spec {
  describe("A RotatingCounter") {
    it("should calculate the sum of events over a period of time") {
      val counter = RotatingCounter((1000, 10))
      for (i <- 1 to 10) {
        counter += 1
        Thread.sleep(10)
      }
      assert(counter() == 10)
    }

    it("should clear all partitions if last event is older than the counter's period") {
      val counter = RotatingCounter((100, 10))
      counter += 1
      Thread.sleep(90)
      assert(counter() == 1)
      Thread.sleep(20)
      assert(counter() == 0)
    }

    it("should clear some partitions if last event is older than period/partitions") {
      val counter = RotatingCounter((100, 10))
      counter += 1
      Thread.sleep(20)
      counter += 2
      assert(counter() == 3)
      Thread.sleep(90)
      assert(counter() == 2)
      Thread.sleep(20)
      assert(counter() == 0)
    }

    it("should return the partitions starting from the oldest partition") {
      val counter = RotatingCounter((80, 2), (80, 2))
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
  }
}
