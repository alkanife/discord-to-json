<h1 align="center">
  discord-to-json
  <br>
  <a href="https://github.com/alkanife/alkabot/blob/main/pom.xml">
    <img src="https://img.shields.io/badge/Open%20JDK-17-green" alt="JDK 17">
  </a>
  <a href="https://github.com/alkanife/alkabot/blob/main/LICENSE">
    <img src="https://img.shields.io/github/license/alkanife/discord-to-json?" alt="LICENSE">
  </a>
  <a href="https://github.com/alkanife/alkabot/releases/tag/1.0.0">
    <img src="https://img.shields.io/badge/version-1.0.0-blue" alt="version">
  </a>
</h1>

<p align="center">
  <b><a href="#overview">Overview</a></b>
  •
  <a href="#usage">Usage</a>
  •
  <a href="#project-dependencies">Project dependencies</a>
  •
  <a href="#license">License</a>
</p>

## Overview
Export Discord messages to JSON

Disclaimer: this is probably very much against Discord's TOS. I am not responsible if you have any issues with Discord after using this program. Use it with knowledge of the consequences it may have, and use it for personal, harmless, non-commercial purposes.

## Usage
```
java -jar discord-to-json.jar [options]
  Options:
    --channel, -c
      The Discord channel id, only required if not using URL
    --delay, -d
      The time between each download cycle
      Default: 5000
    --guild, -g
      The Discord guild id, only required if not using URL
    --help, -h
      Print usage
      Default: false
    --limit, -l
      The number of messages downloaded each cycle
      Default: 70
    --message, -m
      The first message's URL
      (https://discord.com/channels/guildId/channelId/messageId)
    --messageid, -mid
      The first message's id, only required if not using URL
    --output, -o
      Output file name. Will use {date}.json by default
    --replace, -r
      Replace output file if already existing
      Default: false
    --token, -t
      Discord token
    --tokenFile, -tf
      File containing the Discord token
      Default: token
    --debug, --verbose, -D
      Debug/verbose mode
      Default: false
    --version, -v
      Print usage
      Default: false
```

## Project dependencies
This project requires **Java 17+**.

- [DV8FromTheWorld/**JDA**](https://github.com/DV8FromTheWorld/JDA)
- [**logback**](https://github.com/qos-ch/logback)
- [**jCommander**](https://jcommander.org)
- [**lombok**](https://projectlombok.org)
- [**gson**](https://github.com/google/gson)

## License
Under the [MIT](https://github.com/alkanife/discord-to-json/blob/main/LICENSE) license.
