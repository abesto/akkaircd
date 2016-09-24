package net.abesto.akkaircd

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config

class SettingsImpl(config: Config) extends Extension {
  object Tcp {
    val Host: String = config.getString("akkaircd.tcp.host")
    val Port: Int = config.getInt("akkaircd.tcp.port")
  }
}

object Settings extends ExtensionId[SettingsImpl] with ExtensionIdProvider {
  override def lookup = Settings

  override def createExtension(system: ExtendedActorSystem) =
    new SettingsImpl(system.settings.config)
}
