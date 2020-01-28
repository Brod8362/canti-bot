package score.discord.generalbot.command

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import score.discord.generalbot.functionality.pagination.{PaginatedMessage, PaginatedMessages}

class PaginationTestCommand(implicit paginatedMessages: PaginatedMessages) extends Command.Anyone {
  override def name: String = "ptc"

  override def aliases: Seq[String] = Seq("pagination")

  override def description: String = "aa"

  override def execute(message: Message, args: String): Unit = {
    val data = Vector("1","2","3","4","a","b","c","d","x","y","z")
    PaginatedMessage(message.getChannel, a, data, 4)
  }

  def a(eb: EmbedBuilder): Unit = {
    eb.setTitle("pagination test")
  }
}
