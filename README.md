# BetterPlayer
An open-source music bot for Discord written in Java.

## Running BetterPlayer
Requirments:
- Runtime which can run Docker containers
- MySQL Database
- API Key for the YouTube Data API
- Discord Bot

For now you'll have to build the Docker images yourself, using e.g ``docker build -t betterplayer:latest .`` while being in the root of this repository.

### Required environmental variables
`BOT_TOKEN` (String): Your Discord bot token  
`USE_GOOGLE_API` (Boolean): Whether to use the Google APIs for search or use the YouTube Music frontend  
`GOOGLE_API_KEY` (String): Google API key, required regardless of the value of `USE_GOOGLE_API`  
`DB_HOST`: Your database host  
`DB_NAME`: The name of the database  
`DB_USERNAME`: Username for logging into the database  
`DB_PASSWORD`: Password for logging into the database  

## Progress
You can find what is being worked on [here](https://trello.com/b/2n8vzaSp/betterplayer)

## Java dependencies
- [JDA by DV8FromTheWorld](https://github.com/DV8FromTheWorld/JDA)
- [LavaPlayer by Sedmelluq](https://github.com/sedmelluq/lavaplayer)
- [Snakeyaml by Andrey Somov](https://bitbucket.org/asomov/snakeyaml/src/master/)
- [JSON by Apache Foundation](https://mvnrepository.com/artifact/org.json/json)
- [Apache Commons Lang 3 by Apache Foundation](https://commons.apache.org/proper/commons-lang/)
- [HttpLib by me](https://github.com/TheDutchMC/HttpLib)

## Proprietary dependencies
BetterPlayer uses two proprietary dependencies: BetterPlayerAuth and LibBetterPlayer. You don't need these dependencies to work on the code, to compile it or to run it.

## Licence
You may use the source code of this bot for non-profit, private uses. You may not seek to make any profit whatsoever off of this bot without explicit written consent. Any changes you make to the code must be submitted back in the form of a Pull Request.

When using this code in other other non-profit projects, you must include this license and include a link to this repository

