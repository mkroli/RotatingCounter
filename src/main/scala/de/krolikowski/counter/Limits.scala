package de.krolikowski.counter

trait Limits extends RotatingCounter {
  private var limits: List[(Long, () => Unit)] = List()

  def limit(limit: Long)(f: => Unit) {
    limits = (limit, () => f) :: limits
  }

  abstract override def add(count: Long) {
    val oldSum = sum
    super.add(count)
    for (element <- limits) {
      val (limit, callback) = element
      if (limit > oldSum && limit <= (oldSum + count))
        callback()
    }
  }
}
