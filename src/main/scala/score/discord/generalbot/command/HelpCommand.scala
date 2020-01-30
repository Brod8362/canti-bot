package score.discord.generalbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import score.discord.generalbot.collections.ReplyCache
import score.discord.generalbot.functionality.Commands
import score.discord.generalbot.functionality.ownership.MessageOwnership
import score.discord.generalbot.functionality.pagination.{PaginatedMessage, PaginatedMessages, PaginatedStrings}
import score.discord.generalbot.util.{BotMessages, IntStr}
import score.discord.generalbot.wrappers.jda.ID
import scala.concurrent.ExecutionContext.Implicits.global

class HelpCommand(commands: Commands)(implicit val messageOwnership: MessageOwnership, val replyCache: ReplyCache, paginatedMessages: PaginatedMessages) extends Command.Anyone {
  val pageSize = 10

  override def name = "help"

  override def aliases = List("h")

  override def description = "Show descriptions for all commands, or view one command in detail"

  override def execute(message: Message, args: String): Unit = {
    args.trim match {
      case "" => showHelpPage(message, 1)
      case IntStr(page) => showHelpPage(message, page)
      case cmdName => showCommandHelp(message, cmdName)
    }
  }

  private def showCommandHelp(message: Message, invocation: String) = {
    commands.get(invocation) match {
      case Some(command) =>
        val validCommands = commands.all.filter(_ checkPermission message)
        val cmdIndex = validCommands.indexOf(command)
        val rawData = validCommands.map(createExpandedHelpInfo).toVector
        val paginatedStrings = new PaginatedStrings(rawData, 1, cmdIndex)
        PaginatedMessage(message, addExtraEmbedInfo, paginatedStrings)
      case None =>
        //can't find
    }

  }

  private def showHelpPage(message: Message, page: Int): Unit = {
    val myCommands = commands.all.filter(_ checkPermission message)
    val paginatedStrings = new PaginatedStrings(myCommands.map(createCommandPreviewString).toVector, pageSize, page - 1)
    PaginatedMessage(message, addExtraEmbedInfo, paginatedStrings)
  }

  private def createCommandPreviewString(command: Command): String = {
    s"`${commands.prefix}${command.name}`: ${command.description}"
  }

  private def createExpandedHelpInfo(command: Command): String = {
    s"""**Names:** `${(List(command.name) ++ command.aliases).mkString("`, `")}`
       |**Restrictions:** ${command.permissionMessage}
       |${command.description}
       |
       |${command.longDescription(commands.prefix + command.name)}""".stripMargin.trim
  }

  private def addExtraEmbedInfo(embedBuilder: EmbedBuilder): Unit = {
    embedBuilder.setTitle("Command help")
  }
}
