package score.discord.generalbot.command

import net.dv8tion.jda.api.entities.{Message, User}
import score.discord.generalbot.GeneralBot
import score.discord.generalbot.wrappers.jda.Conversions._
import score.discord.generalbot.wrappers.jda.ID

import scala.concurrent.{Await, TimeoutException}
import scala.concurrent.duration._

class StopCommand(bot: GeneralBot, val userId: ID[User]) extends Command.OneUserOnly {
  override def name = "stop"

  override def aliases = List("shutdown")

  override def description = "Shut the bot down"

  override def execute(message: Message, args: String): Unit = {
    // Wait a little to add the reaction, but give up quickly as shutting down is more important
    try {
      Await.ready(message.addReaction("👌").queueFuture(), 300.millis)
    } catch {
      case _: TimeoutException =>
    }
    bot.stop()
  }
}
