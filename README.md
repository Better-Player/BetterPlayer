# BetterPlayer
An open-source music bot for Discord written in Java.

## Features
- Play music from multiple multiple sources
- Customizable, and expandable by providing an easy to use API to add new commands.

## Progress
You can find what is being worked on [here](https://trello.com/b/2n8vzaSp/betterplayer)

## Developer documentation
Some help for those developers out there

### Adding a new command
Adding a new command is easy, all you need is a Class which implements CommandExecutor, and then register your executor!
```java
public class FunCommandExecutor implements CommandExecutor {
  @Override
  public void fireCommand(BetterPlayer betterPlayer, CommandParameters parameters) {
    //Do magic!
  }
}
```
and register it:
```java
BetterPlayer betterPlayer = BetterPlayer.getBetterPlayer();
CommandManager commandManager = betterPlayer.getCommandManager();
commandManager.register("funcommand", new FunCommandExecutor(), "This is a fun command!");
```
>Note: I have not yet written an easy way to add your own extensions by means of 'plugins'. For now you're best of compiling your extension into BetterPlayer.

and done!

## Java dependencies
- [JDA by DV8FromTheWorld](https://github.com/DV8FromTheWorld/JDA)
- [LavaPlayer by Sedmelluq](https://github.com/sedmelluq/lavaplayer)
- [Snakeyaml by Andrey Somov](https://bitbucket.org/asomov/snakeyaml/src/master/)
- [JSON by Apache Foundation](https://mvnrepository.com/artifact/org.json/json)
- [Apache Commons Lang 3 by Apache Foundation](https://commons.apache.org/proper/commons-lang/)
- [HttpLib by me](https://github.com/TheDutchMC/HttpLib)

## C++ Dependencies
- Java JNI Headers
- libsamplerate