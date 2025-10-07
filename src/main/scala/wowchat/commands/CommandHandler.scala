package wowchat.commands

import com.typesafe.scalalogging.StrictLogging
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import wowchat.common.Global
import wowchat.discord.Discord
import wowchat.game.{GamePackets, GameResources, GuildInfo, GuildMember}

import scala.collection.mutable
import scala.util.Try

case class WhoRequest(messageChannel: MessageChannel, playerName: String)
case class WhoResponse(playerName: String, guildName: String, lvl: Int, cls: String, race: String, gender: Option[String], zone: String)

object CommandHandler extends StrictLogging {

  private val NOT_ONLINE = "Bot is not online."
  private val NOT_ALLOWED = "Bot does not have permission to run that command in this channel."

  // make some of these configurable
  private val trigger = "?"

  // gross. rewrite
  var whoRequest: WhoRequest = _

  // returns back the message as an option if unhandled
  // needs to be refactored into a Map[String, <Intelligent Command Handler Function>]
  def apply(fromChannel: MessageChannel, message: String): Boolean = {
    if (!message.startsWith(trigger)) {
      return false
    }

    val splt = message.substring(trigger.length).split(" ")
    val possibleCommand = splt(0).toLowerCase
    val arguments = if (splt.length > 1 && splt(1).length <= 16) Some(splt(1)) else None
    val incChannel = fromChannel.getName.toLowerCase
// TODO: make this into a map, allow help command to spit out available commands dynamically
    Try {
      possibleCommand match {
        case "who" | "online" =>
          if (Global.config.discord.enableWhoGmotdChannels.isEmpty || Global.config.discord.enableWhoGmotdChannels.contains(incChannel)) {
            Global.game.fold({
              Discord.sendMessage(fromChannel, NOT_ONLINE)
              return true
            })(game => {
              val whoSucceeded = game.handleWho(arguments)
              if (arguments.isDefined) {
                whoRequest = WhoRequest(fromChannel, arguments.get)
              }
              whoSucceeded
            })
          } else {
            Discord.sendMessage(fromChannel, NOT_ALLOWED)
            return true
          }
        case "gmotd" =>
          if (Global.config.discord.enableWhoGmotdChannels.isEmpty || Global.config.discord.enableWhoGmotdChannels.contains(incChannel)) {
            Global.game.fold({
              Discord.sendMessage(fromChannel, NOT_ONLINE)
              return true
            })(_.handleGmotd())
          } else {
            Discord.sendMessage(fromChannel, NOT_ALLOWED)
            return true
          }
        case "setgmotd" | "gmotdset" | "setmotd" | "motdset" =>
          if (Global.config.discord.enableSetGmotdChannels.contains(incChannel)) {
            val newMotd = if (splt.length > 1) splt.tail.mkString(" ") else ""
            Global.game.fold({
              Discord.sendMessage(fromChannel, NOT_ONLINE)
              return true
            })(_.handleSetGmotd(newMotd))
          } else {
            Discord.sendMessage(fromChannel, NOT_ALLOWED)
            return true
          }
        case "invite" | "inv" | "ginvite" =>
          if (Global.config.discord.enableInviteChannels.contains(incChannel)) {
            Discord.sendMessage(fromChannel, s"Invite sent: ${splt(1)}")
            Global.game.fold({
              Discord.sendMessage(fromChannel, NOT_ONLINE)
              return true
            })(_.handleGuildInvite(splt(1)))
          } else {
            Discord.sendMessage(fromChannel, NOT_ALLOWED)
            return true
          }
        case "gkick" =>
          if (Global.config.discord.enableKickChannels.contains(incChannel)) {
            Discord.sendMessage(fromChannel, s"Kick sent: ${splt(1)}")
            Global.game.fold({
              Discord.sendMessage(fromChannel, NOT_ONLINE)
              return true
            })(_.handleGuildKick(splt(1)))
          } else {
            Discord.sendMessage(fromChannel, NOT_ALLOWED)
            return true
          }
        case "gpromote" | "promote" =>
          if (Global.config.discord.enablePromoteChannels.contains(incChannel)) {
            Discord.sendMessage(fromChannel, s"Promote sent: ${splt(1)}")
            Global.game.fold({
              Discord.sendMessage(fromChannel, NOT_ONLINE)
              return true
            })(_.handleGuildPromote(splt(1)))
          } else {
            Discord.sendMessage(fromChannel, NOT_ALLOWED)
            return true
          }
        case "gdemote" | "demote" =>
          if (Global.config.discord.enableDemoteChannels.contains(incChannel)) {
            Discord.sendMessage(fromChannel, s"Demote sent: ${splt(1)}")
            Global.game.fold({
              Discord.sendMessage(fromChannel, NOT_ONLINE)
              return true
            })(_.handleGuildDemote(splt(1)))
          } else {
            Discord.sendMessage(fromChannel, NOT_ALLOWED)
            return true
          }
// i dont like it, but it works...
        case "help" | "commands" =>
          val allowedCommands = Seq(
            ("help", "- `?help`/`?commands` = Shows commands available to channel the command is sent in"),
            ("who", "- `?who`/`?online` = Get list of currently online guild members, their level, and their in-game zone/location"),
            ("gmotd", "- `?gmotd` = Get current Guild Message of the Day"),
            ("setgmotd", "- `?setgmotd Message`/`?setmotd Message`/`?gmotdset Message`/`?motdset Message` = Set/Clear the Guild Message of the Day (max 127 chars)\n  - ***CAUTION:*** Using without message content **WILL** clear the current GMotD"),
            ("invite", "- `?invite CharName`/`?inv CharName`/`?ginvite CharName` = Invite `CharName` to join the guild"),
            ("gkick", "- `?gkick CharName` = Kick `CharName` from the guild"),
            ("gpromote", "- `?gpromote CharName`/`?promote CharName` = Promote `CharName` (cannot promote higher than bot's rank in guild)"),
            ("gdemote", "- `?gdemote CharName`/`?demote CharName` = Demote `CharName`\n  - `?promote` & `?demote` only do 1 rank at a time; check bot messages in the configured channel(s) to see the current rank")
          ).filter {
            case (cmd, _) =>
              cmd match {
                case "who" | "online" | "gmotd" =>
                  Global.config.discord.enableWhoGmotdChannels.isEmpty ||
                    Global.config.discord.enableWhoGmotdChannels.contains(incChannel)
                case "setgmotd" =>
                  Global.config.discord.enableSetGmotdChannels.contains(incChannel)
                case "invite" =>
                  Global.config.discord.enableInviteChannels.contains(incChannel)
                case "gkick" =>
                  Global.config.discord.enableKickChannels.contains(incChannel)
                case "gpromote" =>
                  Global.config.discord.enablePromoteChannels.contains(incChannel)
                case "gdemote" =>
                  Global.config.discord.enableDemoteChannels.contains(incChannel)
                case _ => false
              }
          }.map(_._2)

          val helpMessage =
            if (allowedCommands.isEmpty) "No commands available in this channel."
            else "Available commands:\n" + allowedCommands.mkString("\n")

          Discord.sendMessage(fromChannel, helpMessage)
          return true

      }
    }.fold(throwable => {
      // command not found, should send to wow chat
      false
    }, opt => {
      // command found, do not send to wow chat
      if (opt.isDefined) {
        Discord.sendMessage(fromChannel, opt.get)
      }
      true
    })
  }

  // eww
  def handleWhoResponse(whoResponse: Option[WhoResponse],
                        guildInfo: GuildInfo,
                        guildRoster: mutable.Map[Long, GuildMember],
                        guildRosterMatcherFunc: GuildMember => Boolean): Iterable[String] = {
    whoResponse.map(r => {
      Seq(s"${r.playerName} ${if (r.guildName.nonEmpty) s"<${r.guildName}> " else ""}is a level ${r.lvl}${r.gender.fold(" ")(g => s" $g ")}${r.race} ${r.cls} currently in ${r.zone}.")
    }).getOrElse({
      guildRoster
        .values
        .filter(guildRosterMatcherFunc)
        .map(guildMember => {
          val cls = new GamePackets{}.Classes.valueOf(guildMember.charClass) // ... should really move that out
          val days = guildMember.lastLogoff.toInt
          val hours = ((guildMember.lastLogoff * 24) % 24).toInt
          val minutes = ((guildMember.lastLogoff * 24 * 60) % 60).toInt
          val minutesStr = s" $minutes minute${if (minutes != 1) "s" else ""}"
          val hoursStr = if (hours > 0) s" $hours hour${if (hours != 1) "s" else ""}," else ""
          val daysStr = if (days > 0) s" $days day${if (days != 1) "s" else ""}," else ""

          val guildNameStr = if (guildInfo != null) {
            s" <${guildInfo.name}>"
          } else {
            // Welp, some servers don't set guild guid in character selection packet.
            // The only other way to get this information is through parsing SMSG_UPDATE_OBJECT
            // and its compressed version which is quite annoying especially across expansions.
            ""
          }

          s"${guildMember.name}$guildNameStr is a level ${guildMember.level} $cls currently offline. " +
            s"Last seen$daysStr$hoursStr$minutesStr ago in ${GameResources.AREA.getOrElse(guildMember.zoneId, "Unknown Zone")}."
        })
    })
  }
}
