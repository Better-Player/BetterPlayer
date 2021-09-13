# BetterPlayer
An open-source music bot for Discord written in Java.

You can add BetterPlayer to your Discord server by going to [https://join.betterplayer.net](https://join.betterplayer.net), alternatively you can choose to host your own instance of BetterPlayer 

If you want to use the Beta version of BetterPlayer, or have a second instance of BetterPlayer available in your server, you can add BetterPlayer Canary at [https://canary.betterplayer.net](https://canary.betterplayer.net)

## Commands
The default prefix is `$`. To see a list of all commands, their shorthands and what they do use `$help` in a channel where BetterPlayer can send and receive messages

## Running BetterPlayer  yourself
Requirments:
- Runtime which can run Docker containers
- MySQL Database
- API Key for the YouTube Data API
- Discord Bot token

A prebuild image is available at `docker-registry.k8s.array21.dev/betterplayer-bot`, only the `latest` tag is supported for now.

### Environmental variables
#### Required
- `BOT_TOKEN` Your Discord bot token  
- `GOOGLE_API_KEY` Google API key, required regardless of the value of 'USE_GOOGLE_API'  
- `DB_HOST` Your database host  
- `DB_DATABASE` The name of the database  
- `DB_USERNAME` Username for logging into the database  
- `DB_PASSWORD` Password for logging into the database  

#### Optional
- `USE_GOOGLE_API` Whether to use the Google APIs for search or use the YouTube Music frontend, defaults to using the API (true)
- `KSOFT_API_TOKEN` Your [KSoft.Si](https://api.ksoft.si/) API token, required for using the `lyrics` command

## Progress
You can find what is being worked on [here](https://trello.com/b/2n8vzaSp/betterplayer)

## Dependencies
- [JDA](https://github.com/DV8FromTheWorld/JDA)
- [Guava](https://github.com/google/guava)
- [SLF4j](https://github.com/qos-ch/slf4j)
- [LavaPlayer](https://github.com/sedmelluq/lavaplayer)
- [org.json](https://mvnrepository.com/artifact/org.json/json)
- [Gson](https://github.com/google/gson)
- [commons-lang3](https://commons.apache.org/proper/commons-lang/)
- [httplib](https://github.com/TheDutchMC/HttpLib)
- [JDBD](https://github.com/TheDutchMC/JDBD)
