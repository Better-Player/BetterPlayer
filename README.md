# BetterPlayer
An open-source music bot for Discord written in Java.

## Features
- Play music from multiple multiple sources
- Customizable, and expandable by providing an easy to use API to add new commands.

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
commandManager.register("funcommand", new FunCommandExecutor());
```
and done!
