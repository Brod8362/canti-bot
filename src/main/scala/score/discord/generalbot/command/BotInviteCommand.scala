package score.discord.generalbot.command

import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import score.discord.generalbot.collections.ReplyCache
import score.discord.generalbot.functionality.ownership.MessageOwnership
import score.discord.generalbot.util.BotMessages
import score.discord.generalbot.wrappers.jda.Conversions._

class BotInviteCommand(implicit messageOwnership: MessageOwnership, replyCache: ReplyCache) extends Command.Anyone {
  override def name = "botinvite"

  override def aliases = Nil

  override def description = "Get a link to invite this bot to your server"

  override def execute(message: Message, args: String) = {
    import Permission._
    message reply BotMessages.plain(message.getJDA.getInviteUrl(
      MANAGE_ROLES, MANAGE_CHANNEL, MESSAGE_MANAGE, VOICE_MOVE_OTHERS
    ))
  }
}
