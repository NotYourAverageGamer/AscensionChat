package wowchat.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.netty.channel.EventLoopGroup
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import wowchat.discord.Discord
import wowchat.game.GameCommandHandler

import scala.collection.mutable

object Global {

  // Event loop group for managing multiple channels in Netty
  var group: EventLoopGroup = _
  // Configuration object for the WowChat application
  var config: WowChatConfig = _

  // Discord bot instance
  var discord: Discord = _
  // Optional GameCommandHandler instance for game-related commands
  var game: Option[GameCommandHandler] = None

  // Mappings for chat channel configurations between Discord and World of Warcraft
  val discordToWow = new mutable.HashMap[String, mutable.Set[WowChannelConfig]]
    with mutable.MultiMap[String, WowChannelConfig]
  val wowToDiscord = new mutable.HashMap[(Byte, Option[String]), mutable.Set[(TextChannel, DiscordChannelConfig)]]
    with mutable.MultiMap[(Byte, Option[String]), (TextChannel, DiscordChannelConfig)]
  val guildEventsToDiscord = new mutable.HashMap[String, mutable.Set[TextChannel]]
    with mutable.MultiMap[String, TextChannel]

  // Method to get the current time as a formatted string
  def getTime: String = {
    LocalDateTime.now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
  }
}
