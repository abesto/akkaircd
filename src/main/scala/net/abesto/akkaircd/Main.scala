package net.abesto.akkaircd

import net.abesto.akkaircd.actors.MainActor

object Main {
  def main(args: Array[String]): Unit = {
    val initialActor = classOf[MainActor].getName
    akka.Main.main(Array(initialActor))
  }
}
