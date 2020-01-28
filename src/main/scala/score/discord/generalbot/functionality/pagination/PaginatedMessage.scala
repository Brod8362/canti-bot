package score.discord.generalbot.functionality.pagination

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.{Message, MessageChannel}
import score.discord.generalbot.util.APIHelper
import score.discord.generalbot.wrappers.jda.RichRestAction

import scala.concurrent.Future
import scala.util.chaining._
import scala.concurrent.ExecutionContext.Implicits.global

object PaginatedMessage {
  def apply(channel: MessageChannel, addExtraMessageElements: EmbedBuilder => Unit, data: IndexedSeq[String],
            linesPerPage: Int, startingPage: Int)(implicit paginatedMessages: PaginatedMessages): Future[PaginatedMessage] = {
    val paginatedStrings = new PaginatedStrings(data, linesPerPage, startingPage)
    val embedBuilder = new EmbedBuilder()
    addExtraMessageElements(embedBuilder)
    embedBuilder.setDescription(paginatedStrings.getCurrentPage)
    val messageFuture = new RichRestAction(channel.sendMessage(embedBuilder.build())).queueFuture()
    val paginatedFuture = messageFuture.map(new PaginatedMessage(_, addExtraMessageElements, paginatedStrings))
    paginatedMessages(paginatedFuture)
    paginatedFuture
  }

  def apply(channel: MessageChannel, addExtraMessageElements: EmbedBuilder => Unit, data: IndexedSeq[String],
            linesPerPage: Int)(implicit paginatedMessages: PaginatedMessages): Future[PaginatedMessage] = {
    apply(channel, addExtraMessageElements, data, linesPerPage, 0)
  }
}

class PaginatedMessage(val message: Message, addExtraMessageElements: EmbedBuilder => Unit,
                       paginatedString: PaginatedStrings) {

  message.addReaction("⬅").queue()
  message.addReaction("➡").queue()

  def next(): Unit = {
    paginatedString.next()
    updateMessage()
  }

  def prev(): Unit = {
    paginatedString.prev()
    updateMessage()
  }

  private def makeMessageContent() = {
    new EmbedBuilder()
      .setDescription(paginatedString.getCurrentPage)
      .setFooter(s"Page: ${paginatedString.currentPage + 1}/${paginatedString.maxPages}")
      .tap(addExtraMessageElements)
      .build
  }

  def updateMessage(): Unit = {
    APIHelper.tryRequest(
      message.editMessage(makeMessageContent()),
      onFail = APIHelper.loudFailure("editing paginated message", message.getChannel)
    )
  }
}
