package score.discord.generalbot.functionality.pagination

import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.hooks.EventListener
import score.discord.generalbot.functionality.ownership.MessageOwnership
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
        val messageOption = messages.get(e.getMessageIdLong)
        messageOption match { //TODO anybody can use the reactions, should only be the message owner
          case Some(messageFuture) =>
            for (paginatedMessage <- messageFuture) {
              e.getReactionEmote.getEmoji match {
                case "➡" =>
                  paginatedMessage.next()
                case "⬅" =>
                  paginatedMessage.prev()
                case _ =>
              }
            }
          case None => //do nothing, because this message isn't paginated
        }
      case _ => //some other event we don't care about
    }
  }

  def apply(paginatedMessageFuture: Future[PaginatedMessage]): Unit = {
    for (paginatedMessage <- paginatedMessageFuture) {
      messages.put(paginatedMessage.message.getIdLong, paginatedMessageFuture)
    }
    scheduler.schedule(60 seconds) {
      removeOldMessage(paginatedMessageFuture)
    }
  }

  def get(id: Long): Option[Future[PaginatedMessage]] = messages.get(id)

  private def removeOldMessage(paginatedMessageFuture: Future[PaginatedMessage]): Unit = {
    for (paginatedMessage <- paginatedMessageFuture) {
      paginatedMessage.message.clearReactions().queue()
      messages.remove(paginatedMessage.message.getIdLong)
    }
  }
}
