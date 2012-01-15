package de.krolikowski.counter

/**
 * This trait adds some shortcuts to
 * [[de.krolikowski.counter.RotatingCounter]].
 */
trait Shortcuts extends RotatingCounter {
  /**
   * A shortcut for add(1)
   */
  final def add: Unit = add(1)

  /**
   * A shortcut for add(count)
   */
  def +=(count: Long) = add(count)

  /**
   * A shortcut for sum
   */
  def apply() = sum

  override def toString =
    classOf[RotatingCounter].getSimpleName() +
      "(sum = " + sum + ", partitions = " + partitions + ")"
}
