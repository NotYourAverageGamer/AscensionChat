# AscensionChat

AscensionChat is a Discord integration chat bot for **Project Ascension**, originally forked from **WoWChat** by fjaros. My _(NotYourAverageGamer)_ fork also contains code sourced from **szyler** and **xan-asc** — thank you all!

**Note:** This fork has been modified for use with Project Ascension. While the code is still technically there, changes will need to be made if you want to use this with other servers _(perhaps take a look at one of the forks mentioned above, which should provide a better starting point)_. It does NOT support WoW Classic or Retail servers.

**New Requirement:** Due to Discord changes, you must check `PRESENCE INTENT`, `SERVER MEMBERS INTENT` AND `MESSAGE CONTENT INTENT` under the `Privileged Gateway Intents` section in the Discord developer portal.

Currently supported version of this fork:

- Ascension WoW 3.3.5

## Features

- **Clientless**: Does not need the WoW Client to be open to run
- **Seamless Chat Integration**: Integrates channels between WoW and Discord
  - Guild chat, Officer chat, Local chat, Emotes, Custom Channels _(Local chat, Emotes, and Custom Channels are disabled by default)_
  - In-game links _(items, spells, etc.)_ are displayed as links to the Ascension Database _(db.ascension.gg)_
  - Configurable message format
- **Smart Tagging**:
  - Tag players on Discord from WoW using `@and_part_or_all_of_their_name`
  - Tag `@here`, `@everyone`, and `"@Even Roles With Spaces"` _(include quotes around them)_
- **Custom Commands**:
  - Check who is online in your guild with `?who`
  - Query other players in the world
- **Highly Portable**:
  - Runs as a Java program and works on **Windows**, **Mac**, and **Linux**

## How it Works

The bot uses Discord's API to log into your Discord server. It then uses supplied information to log into Ascension as a WoW character. Once it logs into WoW and sees the configured channels, it relays messages to your Discord and WoW channels respectively.

### Important: Do not use this bot on an account with existing characters!

Even though this bot does not do anything malicious, some servers may not like a bot connecting, and GMs may ban the **_account_**! Make a new account just for the bot. **_YOU HAVE BEEN WARNED!_**

#### Example 1 _(before fixing achievement whitespace)_

![gd-echoes](https://raw.githubusercontent.com/fjaros/wowchat/master/images/example1.png)

##### Talking in Guild Chat

![guild-chat-construct](https://raw.githubusercontent.com/fjaros/wowchat/master/images/example2.png)

## Setup Discord Bot

1. Create a Discord Bot on your Discord account:

   - Go to <https://discordapp.com/developers/applications/>
   - Sign into your Discord account if necessary, and click `Create an application`
   - Change the application name to something meaningful like "WoW Chat"
   - On the left, click the `Bot` tab
   - Add a Bot
   - Disable the `Public Bot` option
   - Enable `PRESENCE INTENT`, `SERVER MEMBERS INTENT` and `MESSAGE CONTENT INTENT` under `Privileged Gateway Intents`. **This is important! Without it, your bot will not work!**
   - Underneath where it says `TOKEN`, click `Copy`. _(If you can't see a copy option, click reset then copy)_ This is what Ascension Chat will use to log into Discord.

2. Invite your bot to Discord:
   - Go back to <https://discordapp.com/developers/applications/> and click your new Bot application.
   - In the browser, enter: <https://discordapp.com/oauth2/authorize?client_id=CLIENT_ID&scope=bot>
     - Replace **`CLIENT_ID`** (the one in CAPS) in the URL with the token from the Discord applications page earlier.
   - Assign the bot the necessary Discord roles/permissions to view/enter your desired channels.

## Configure ascensionchat.conf

1. Configure AscensionChat by opening `ascensionchat.conf` in your preferred text editor. You can also create your own file using the supplied `ascensionchat.conf` as a template.

   - **Discord** section:
     - `token`: Paste the `Bot token` you copied above.
     - `enable_dot_commands`: If set to `1`, it will not format outgoing messages starting with `.`, enabling you to send things like `.s in` to the server directly. If set to `0`, it will format these messages like regular messages.
     - `dot_commands_whitelist`: If empty, it will allow or disallow dot commands based on the `enable_dot_commands` setting above. If any command is listed here, the bot will **ONLY** allow _those specific commands_ to be sent in-game.
     - `enable_invite_command`: If set to `1`, it will allow the use of `?invite charname` anywhere `?who` is allowed. If set to `0`, it disables the command.
     - `banned_invite_list`: A list of character names that cannot be used with `?invite`
     - `enable_commands_channels`: A list of channels for which to allow commands. If not specified or empty, the bot will allow commands from all channels.
   - **WoW** section:
     - `platform`: Leave as `Mac` unless your target server has Warden _(anticheat)_ disabled AND it is blocking/has disabled Mac logins. In this case, put `Windows`.
       - **NOTE:** For Ascension, I cannot get past character selection if `Mac` is set on MacOS. Having `platform=Windows` seems to work without issue.
     - `locale`: Optionally specify a locale if you want to join locale-specific global channels. **enUS** is the default locale.
     - `enable_server_motd`: Set to `0` to ignore sending the server's MotD. Set to `1` to send the server's MotD as a SYSTEM message.
     - `account`: Replace `REPLACE-ME` with the bot's Ascension account name.
     - `password`: Replace `REPLACE-ME` with the bot's WoW Ascension password.
     - `character`: Replace `REPLACE-ME` with the bot's character name, as it is shown in the character list.
   - **Guild** section:
     - This section sets up guild notifications on Discord.
     - For each notification—`online`, `offline`, `joined`, `left`, `motd`, `achievement`—specify:
       - `enabled`: `0` to not display in Discord, `1` to display in Discord
       - `format`: How should the message be displayed?
       - `channel`: Enter the `Channel name` **OR** `ID` of where you want the message displayed, instead of the default `guildrelay-chat` channel.
   - **Chat** section:
     - This section sets up the channel relays between Discord and Ascension. You can have an unlimited number of channel relays.
     - `direction`: How do you want to relay each channel? Put:
       `wow_to_discord`, `discord_to_wow`, or `both`.
     - **wow** section:
       - In `type`, put one of: `Say`, `Guild`, `Officer`, `Emote`, `Yell`, `System`, `Whisper`, `Channel`. This is the type of chat the Bot will read for this section.
         - If you put `type=Channel`, you must also provide a `channel=yourChannelName` value.
       - In `format`, put how you want to display the message. Supported replaceable values are `%time`, `%user`, `%message`, and `%channel` if the above `type` is `Channel`.
       - `filters`: See filters section. If a channel configuration has this section, it will override the global filters and use these instead for this channel.
         - If this is in the `wow` section, it will filter `Discord->WoW` messages.
       - Optionally in `id`, specify the `channel ID` if your server has a non-standard global channel.
     - **Discord** section:
       - `channel`: The Discord channel **name** OR **ID** where to display the message. **It is advised to use the channel ID here instead of the name, so the bot does not stop working when the channel name is changed.**
         - To see channels' IDs, you must enable Developer mode in Discord under User Settings -> Appearance -> Advanced.
       - `format`: Same options as in the **wow** section above.
       - `filters`: See filters section. If a channel configuration has this section, it will override the global filters and use these instead for this channel.
         - If this is in the `discord` section, it will filter `WoW->Discord` messages.
   - **Filters** section:
     - This section specifies filters for chat messages to be ignored by the bot. It works for both directions: Discord to WoW and WoW to Discord. It can be overridden in each specific channel configuration as stated above.
     - `enabled`: `0` to globally disable all filters, `1` to enable them.
     - `patterns`: List of Java Regex match patterns. If the incoming message matches any one of the patterns and filters are enabled, it will be ignored.
       - When ignored, the message will not be relayed; however, it will be logged into the bot's command line output prepended with the word FILTERED.

## Running AscensionChat

1. Download the [**latest**](https://github.com/NotYourAverageGamer/AscensionChat/releases/latest) ready-made binary from the GitHub [releases](https://github.com/NotYourAverageGamer/AscensionChat/releases)

   - **Make sure you have a Java Runtime Environment (JRE) 1.8 or higher installed on your system!**

     - Check the version of your Java installation with

       ```bash
       java -version
       ```

   - **On Windows**: Edit `ascensionchat.conf` as above and run `run.bat`
   - **On Mac/Linux**: Edit `ascensionchat.conf` as above and run `run.sh`

## Compiling AscensionChat from source

1. WoW Chat/AscensionChat is written in Scala and compiles to a Java executable using [**Maven**](https://maven.apache.org).
2. It requires **Java JDK 21+** and [**Scala 2.12.19**](https://www.scala-lang.org/download/2.12.19.html).

   - Check the version of your Java installation with

     ```bash
     java -version
     ```

   - You may need to restart your terminal for the install/upgrade to take effect.

3. Run `mvn clean package` which will produce a file in the target folder called `ascensionchat-3.1.2.zip`
4. Unzip `ascensionchat-3.1.2.zip`, edit the configuration file, and run `run.bat` for Windows or `run.sh` for Linux/MacOS. (Edit the name of the config file in `run.*` if you supply your own config)
   - If no config file is supplied, the bot will try to use `ascensionchat.conf`

## Updating

- Download the [**latest**](https://github.com/NotYourAverageGamer/AscensionChat/releases/latest) `ascensionchat.jar` and replace the one in your current `ascensionchat` folder. Alternatively, download the [**latest**](https://github.com/NotYourAverageGamer/AscensionChat/releases/latest) `ascensionchat.zip` file; but be careful not to replace your `ascensionchat.conf`!
  - Before updating your `ascension.conf` file, make sure to save a copy of your current `.conf` file. This will allow you to easily transfer your login details to the new config, making the update process smoother.

## Tested OS's

#### This project has been manually compiled and run on the following Operating Systems (bare-metal, no VM's/Containers)

- **MacOS**

  - **MacOS Ventura** 13.6.7 (13" Early '15 MacBook Air, Intel, OpenCore Legacy Patcher)
    - [SDKMAN!](https://sdkman.io/) one-line install from their site (MacOS/Linux only)
    - Scala (2.12.19) installed with SDKMAN!

- **Linux**

  - **Arch** (rolling, x86_64, Linux 6.6.37-1-lts)
    - Java (21.0.3-tem), Scala (2.12.19) and Maven (3.9.8) installed with [SDKMAN!](https://sdkman.io/)
  - **Mint** Virginia 21.3 (x86_64, Linux 5.15.0-113-generic)
    - Scala (2.12.19) and Java (21.0.3-tem) installed with [SDKMAN!](https://sdkman.io/)
    - Maven from the `apt` repository

- **Windows**

  - **Windows 10 Pro** (22H2, 19045.4529)
    - You will want to use `Windows Terminal` (_which you might not have by default_), because the default `Windows Console Host` does not support the ANSI Escape Sequences used in this project. This results in the Escape Codes showing in the terminal and no colour, making terminal output harder to read. Fortunately, this is pretty straightforward to setup. See [**HERE**](https://learn.microsoft.com/en-us/windows/terminal/install) for steps to setup/install.
  - **Windows 11 Pro** (23H2, 22631.3737)
  - **WSL - Ubuntu 22.04** (on Win11-Pro)
