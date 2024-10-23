package wowchat

import java.util.concurrent.{Executors, TimeUnit}

import wowchat.common.{CommonConnectionCallback, Global, ReconnectDelay, WowChatConfig}
import wowchat.discord.Discord
import wowchat.game.GameConnector
import wowchat.realm.{RealmConnectionCallback, RealmConnector}

import com.typesafe.scalalogging.StrictLogging
import io.netty.channel.nio.NioEventLoopGroup

import scala.io.Source

object WoWChat extends StrictLogging {

  private val RELEASE = Source.fromResource("version.properties").getLines.next().split("=")(1).trim

  def main(args: Array[String]): Unit = {
    logger.info(s"""${Ansi.BOLD}



${Ansi.GREEN}  .d8b.  .d8888.  .o88b. d88888b d8b   db .d8888. d888888b .d88b.  d8b   db  .o88b. db   db  .d8b.  d888888b
${Ansi.GREEN} d8' `8b 88'  YP d8P  Y8 88'     888o  88 88'  YP   `88'  .8P  Y8. 888o  88 d8P  Y8 88   88 d8' `8b `~~88~~'
${Ansi.GREEN} 88ooo88 `8bo.   8P      88ooooo 88V8o 88 `8bo.      88   88    88 88V8o 88 8P      88ooo88 88ooo88    88   
${Ansi.GREEN} 88~~~88   `Y8b. 8b      88~~~~~ 88 V8o88   `Y8b.    88   88    88 88 V8o88 8b      88~~~88 88~~~88    88   
${Ansi.GREEN} 88   88 db   8D Y8b  d8 88.     88  V888 db   8D   .88.  `8b  d8' 88  V888 Y8b  d8 88   88 88   88    88   
${Ansi.GREEN} YP   YP `8888Y'  `Y88P' Y88888P VP   V8P `8888Y' Y888888P `Y88P'  VP   V8P  `Y88P' YP   YP YP   YP    YP   


                                             ${Ansi.BCYAN}~~>>>${Ansi.CLR}${Ansi.BYELLOW} $RELEASE ${Ansi.BOLD}${Ansi.BCYAN}<<<~~


   ${Ansi.CLR}""")
    val confFile = if (args.nonEmpty) {
      args(0)
    } else {
      logger.info("No configuration file supplied. Trying with default ascensionchat.conf.")
      "ascensionchat.conf"
    }
    Global.config = WowChatConfig(confFile)

    checkForNewVersion

    val gameConnectionController: CommonConnectionCallback = new CommonConnectionCallback {

      private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor
      private val reconnectDelay = new ReconnectDelay

      override def connect: Unit = {
        Global.group = new NioEventLoopGroup

        val realmConnector = new RealmConnector(new RealmConnectionCallback {
          override def success(host: String, port: Int, realmName: String, realmId: Int, sessionKey: Array[Byte]): Unit = {
            gameConnect(host, port, realmName, realmId, sessionKey)
          }

          override def disconnected: Unit = doReconnect

          override def error: Unit = sys.exit(1)
        })

        realmConnector.connect
      }

      private def gameConnect(host: String, port: Int, realmName: String, realmId: Int, sessionKey: Array[Byte]): Unit = {
        new GameConnector(host, port, realmName, realmId, sessionKey, this).connect
      }

      override def connected: Unit = reconnectDelay.reset

      override def disconnected: Unit = doReconnect

      def doReconnect: Unit = {
        Global.group.shutdownGracefully()
        Global.discord.changeRealmStatus("Connecting...")
        val delay = reconnectDelay.getNext
        logger.info(s"${Ansi.RED}Disconnected from server!${Ansi.CLR} Reconnecting in $delay seconds...")

        reconnectExecutor.schedule(new Runnable {
          override def run(): Unit = connect
        }, delay, TimeUnit.SECONDS)
      }
    }

    logger.info(s"${Ansi.BCYAN}Connecting to Discord...${Ansi.CLR}")
    Global.discord = new Discord(new CommonConnectionCallback {
      override def connected: Unit = gameConnectionController.connect

      override def error: Unit = sys.exit(1)
    })
  }

  private def checkForNewVersion = {
    // This is JSON, but I really just didn't want to import a full blown JSON library for one string.
    val data = Source.fromURL("https://api.github.com/repos/NotYourAverageGamer/AscensionChat/releases/latest").mkString
    val regex = "\"tag_name\":\"(.+?)\",".r
    val repoTagName = regex
      .findFirstMatchIn(data)
      .map(_.group(1))
      .getOrElse("NOT FOUND")

if (repoTagName != RELEASE) {
  logger.error(s"""${Ansi.BYELLOW}


   ~~~ ${Ansi.BRED} !!!                   ${Ansi.BCYAN}YOUR AscensionChat VERSION IS OUT OF DATE                  ${Ansi.BRED}!!! ${Ansi.BYELLOW} ~~~
    ~~~ ${Ansi.BRED} !!!                           ${Ansi.BYELLOW}Current Version${Ansi.BWHITE}: ${Ansi.BYELLOW}$RELEASE                          ${Ansi.BRED}!!! ${Ansi.BYELLOW} ~~~
     ~~~ ${Ansi.BRED} !!!                          ${Ansi.BGREEN}Latest  Version${Ansi.BWHITE}: ${Ansi.BGREEN}$repoTagName                        ${Ansi.BRED}!!! ${Ansi.BYELLOW} ~~~
    ~~~ ${Ansi.BRED} !!!    ${Ansi.BWHITE}GO TO ${Ansi.BOLD}${Ansi.BLUE}https://github.com/NotYourAverageGamer/AscensionChat${Ansi.CLR} ${Ansi.BWHITE}TO UPDATE    ${Ansi.BRED}!!! ${Ansi.BYELLOW} ~~~
   ~~~ ${Ansi.BRED} !!!                   ${Ansi.BCYAN}YOUR AscensionChat VERSION IS OUT OF DATE                  ${Ansi.BRED}!!! ${Ansi.BYELLOW} ~~~
    

 ${Ansi.CLR}""")
}
  }
}
