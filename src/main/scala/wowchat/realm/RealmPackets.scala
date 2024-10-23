package wowchat.realm

import wowchat.Ansi

object RealmPackets {
  val CMD_AUTH_LOGON_CHALLENGE = 0x00
  val CMD_AUTH_LOGON_PROOF = 0x01
  val CMD_REALM_LIST = 0x10

  object AuthResult {
    val WOW_SUCCESS = 0x00
    val WOW_SUCCESS_SURVEY = 0x0E

    val WOW_FAIL_BANNED = 0x03
    val WOW_FAIL_UNKNOWN_ACCOUNT = 0x04
    val WOW_FAIL_INCORRECT_PASSWORD = 0x05
    val WOW_FAIL_ALREADY_ONLINE = 0x06
    val WOW_FAIL_NO_TIME = 0x07
    val WOW_FAIL_DB_BUSY = 0x08
    val WOW_FAIL_VERSION_INVALID = 0x09
    val WOW_FAIL_VERSION_UPDATE = 0x0A
    val WOW_FAIL_INVALID_SERVER = 0x0B
    val WOW_FAIL_SUSPENDED = 0x0C
    val WOW_FAIL_FAIL_NOACCESS = 0x0D
    val WOW_FAIL_PARENTCONTROL = 0x0F
    val WOW_FAIL_LOCKED_ENFORCED = 0x10
    val WOW_FAIL_TRIAL_ENDED = 0x11
    val WOW_FAIL_USE_BATTLENET = 0x12
    val WOW_FAIL_ANTI_INDULGENCE = 0x13
    val WOW_FAIL_EXPIRED = 0x14
    val WOW_FAIL_NO_GAME_ACCOUNT = 0x15
    val WOW_FAIL_CHARGEBACK = 0x16
    val WOW_FAIL_INTERNET_GAME_ROOM_WITHOUT_BNET = 0x17
    val WOW_FAIL_GAME_ACCOUNT_LOCKED = 0x18
    val WOW_FAIL_UNLOCKABLE_LOCK = 0x19
    val WOW_FAIL_CONVERSION_REQUIRED = 0x20
    val WOW_FAIL_DISCONNECTED = 0xFF

    def isSuccess(authResult: Int): Boolean = {
      authResult == WOW_SUCCESS || authResult == WOW_SUCCESS_SURVEY
    }

    def getMessage(authResult: Int): String = {
      authResult match {
        case WOW_SUCCESS | WOW_SUCCESS_SURVEY => s"${Ansi.BGREEN}Success!${Ansi.CLR}"
        case WOW_FAIL_BANNED => s"${Ansi.BRED}${Ansi.BOLD}Your account has been banned!${Ansi.CLR}"
        case WOW_FAIL_INCORRECT_PASSWORD => s"${Ansi.BRED}Incorrect username or password!${Ansi.CLR}"
        case WOW_FAIL_UNKNOWN_ACCOUNT => s"${Ansi.BYELLOW}Login failed. Wait a moment and try again!${Ansi.CLR}"
        case WOW_FAIL_ALREADY_ONLINE => s"${Ansi.BYELLOW}Your account is already online. Wait a moment and try again!${Ansi.CLR}"
        case WOW_FAIL_VERSION_INVALID | WOW_FAIL_VERSION_UPDATE => s"${Ansi.BRED}Invalid game version for this server!${Ansi.CLR}"
        case WOW_FAIL_SUSPENDED => s"${Ansi.BRED}Your account has been suspended!${Ansi.CLR}"
        case WOW_FAIL_FAIL_NOACCESS => s"${Ansi.BRED}Login failed! ${Ansi.BYELLOW}You do not have access to this server!${Ansi.CLR}"
        case x => f"${Ansi.BYELLOW}Failed to login to realm server! Error code: $x%02X${Ansi.CLR}"
      }
    }
  }
}
