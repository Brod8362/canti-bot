package score.discord.generalbot.functionality.pagination

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.hooks.EventListener
import score.discord.generalbot.functionality.ownership.MessageOwnership
import score.discord.generalbot.util.APIHelper
import score.discord.generalbot.wrappers.Scheduler

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class PaginatedMessages(implicit scheduler: Scheduler, ownership: MessageOwnership) extends EventListener {

  private val rightArrowEmoji = "➡"
  private val leftArrowEmoji = "⬅"

  private val messages: mutable.Map[Long, Future[PaginatedMessage]] = new mutable.HashMap

  override def onEvent(event: GenericEvent): Unit = {
    event match {
      case e: GenericMessageReactionEvent =>
        val messageOption = get(e.getMessageIdLong)
        messageOption match {
          case Some(messageFuture) =>
            for (paginatedMessage <- messageFuture) {
              if (paginatedMessage.sourceUser == e.getUser) {
                e.getReactionEmote.getEmoji match {
                  case "➡" =>
                    paginatedMessage.next()
                  case "⬅" =>
                    paginatedMessage.prev()
                  case _ =>
                }
              }
            }
          case None => //do nothing, because this message isn't paginated
        }
      case _ => //some other event we don't care about
    }
  }

  /**
    * Adds a Future[PaginatedMessage] into the registry of active paginated messages.
    *
    * @param paginatedMessageFuture The paginated message to be added.
    */
  def apply(paginatedMessageFuture: Future[PaginatedMessage]): Unit = {
    paginatedMessageFuture.map({
      m =>
        messages.put(m.message.getIdLong, paginatedMessageFuture)

        scheduler.schedule(60 seconds) {
          removeOldMessage(paginatedMessageFuture)
        }
    })
  }

  /**
    * Get a paginated message from the ID of the message it represents.
    *
    * @param id The ID of the message
    * @return The paginated message Option
    */
  def get(id: Long): Option[Future[PaginatedMessage]] = messages.get(id)

  private def removeOldMessage(paginatedMessageFuture: Future[PaginatedMessage]): Unit = {
    for (paginatedMessage <- paginatedMessageFuture) {
      APIHelper.tryRequest(
        paginatedMessage.message.clearReactions(),
        onFail = APIHelper.loudFailure("removing paginated message reactions", paginatedMessage.message.getChannel)
      )
      messages.remove(paginatedMessage.message.getIdLong)
    }
  }
}
