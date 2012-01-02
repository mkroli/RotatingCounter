package de.krolikowski

import de.krolikowski.counter.RotatingCounter

package object counter {
  /**
   * A shortcut to [[de.krolikowski.counter.RotatingCounter]]'s sum method.
   */
  implicit def rotatingCounterToLong(rotatingCounter: RotatingCounter) = rotatingCounter.sum

  /**
   * A shortcut to [[de.krolikowski.counter.RotatingCounter]]'s partitions method.
   */
  implicit def rotatingCounterToSeq(rotatingCounter: RotatingCounter) = rotatingCounter.partitions
}
