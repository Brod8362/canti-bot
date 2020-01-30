package score.discord.generalbot.functionality.pagination

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.{Message, MessageChannel}
import score.discord.generalbot.util.APIHelper
import score.discord.generalbot.wrappers.jda.{ID, RichRestAction}
import score.discord.generalbot.wrappers.jda.Conversions._

import scala.concurrent.Future
import scala.util.chaining._
import scala.concurrent.ExecutionContext.Implicits.global

object PaginatedMessage {
  /**
    * Create a new PaginatedMessage. This message will automatically be added to the registry.
    *
    * @param channel                 The channel the PaginatedMessage will be sent in.
    * @param addExtraMessageElements A function that will set desired values of an EmbedBuilder.
    * @param paginatedStrings        The PaginatedStrings object holding the desired data to be displayed.
    * @param paginatedMessages       The registry of PaginatedMessages
    * @return a Future of the PaginatedMessage to be created.
    */
  def apply(channel: MessageChannel, addExtraMessageElements: EmbedBuilder => Unit, paginatedStrings: PaginatedStrings)
           (implicit paginatedMessages: PaginatedMessages): Future[PaginatedMessage] = {
    val embedBuilder = new EmbedBuilder()
    addExtraMessageElements(embedBuilder)
    embedBuilder.setDescription(paginatedStrings.getCurrentPage)
    val messageFuture = new RichRestAction(channel.sendMessage(embedBuilder.build())).queueFuture()
    val paginatedFuture = messageFuture.map(new PaginatedMessage(_, addExtraMessageElements, paginatedStrings))
    paginatedMessages(paginatedFuture)
    paginatedFuture
  }

  def apply(channel: MessageChannel, addExtraMessageElements: EmbedBuilder => Unit, data: IndexedSeq[String],
            linesPerPage: Int, startingPage: Int)(implicit paginatedMessages: PaginatedMessages): Future[PaginatedMessage] = {
    apply(channel, addExtraMessageElements, new PaginatedStrings(data, linesPerPage, startingPage))
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

  val sourceMessageId: ID[Message] = message.id

  /**
    * Advance the content by one page, and update the discord message.
    * If the content is already at the last page, nothing with happen.
    */
  def next(): Unit = {
    paginatedString.next()
    updateMessage()
  }

  /**
    * Rewind the content by one page, and update the discord message.
    * If the content is already at page 1, nothing will happen.
    */
  def prev(): Unit = {
    paginatedString.prev()
    updateMessage()
  }

  /**
    * Set the content to the desired page and update the discord message.
    * If the desired page is outside of the available range, nothing will happen.
    *
    * @param page
    * @return
    */
  def setPage(page: Int): Boolean = {
    if (paginatedString.setPage(page)) {
      updateMessage()
      return true
    }
    false
  }

  private def makeMessageContent() = {
    new EmbedBuilder()
      .setDescription(paginatedString.getCurrentPage)
      .setFooter(s"Page: ${paginatedString.currentPage + 1}/${paginatedString.maxPages}")
      .tap(addExtraMessageElements)
      .build
  }

  /**
    * Edit the discord message to display the current page's content.
    */
  def updateMessage(): Unit = {
    APIHelper.tryRequest(
      message.editMessage(makeMessageContent()),
      onFail = APIHelper.loudFailure("editing paginated message", message.getChannel)
    )
  }
}
