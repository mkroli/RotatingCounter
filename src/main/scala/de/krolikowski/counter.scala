package de.krolikowski

import de.krolikowski.counter.RotatingCounter

package object counter {
  implicit def rotatingCounterToLong(rotatingCounter: RotatingCounter) = rotatingCounter()
}
