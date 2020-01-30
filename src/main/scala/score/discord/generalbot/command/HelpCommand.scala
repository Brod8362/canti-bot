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
      case cmdName => showCommandHelp(cmdName)
    }
  }

  override def executeForEdit(message: Message, myMessageOption: Option[ID[Message]], args: String): Unit = {
    val paginatedMessageOption = paginatedMessages.get(message.getIdLong)
    for (paginatedMessage <- paginatedMessageOption) {
      args.trim match {
        case IntStr(page) => paginatedMessage.map({
          m =>
            m.setPage(page)
            m.updateMessage()
        })
      }
    }
  }

  private def showCommandHelp(command: String) = {
    val unprefixed = command.stripPrefix(commands.prefix)
    commands.get(unprefixed)
      .toRight("Expected a page number or command name, but got something else.")
      .map(command => BotMessages plain
        s"""**Names:** `${(List(command.name) ++ command.aliases).mkString("`, `")}`
           |**Restrictions:** ${command.permissionMessage}
           |${command.description}
           |
           |${command.longDescription(commands.prefix + unprefixed)}""".stripMargin.trim)
  }

  private def showHelpPage(message: Message, page: Int): Unit = {
    val myCommands = commands.all.filter(_ checkPermission message)
    val paginatedStrings = new PaginatedStrings(myCommands.map(createCommandPreviewString).toVector, 10, page - 1)
    PaginatedMessage(message.getChannel, addExtraEmbedInfo, paginatedStrings)
  }

  private def createCommandPreviewString(command: Command): String = {
    s"`${commands.prefix}${command.name}`: ${command.description}\n"
  }

  private def addExtraEmbedInfo(embedBuilder: EmbedBuilder): Unit = {
    embedBuilder.appendDescription("You can erase most replies this bot sends to you by reacting with ❌ or 🚮.")
      .setTitle("Command help")
  }
}
