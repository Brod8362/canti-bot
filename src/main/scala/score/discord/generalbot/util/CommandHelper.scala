package score.discord.generalbot.util

import net.dv8tion.jda.api.entities.{Guild, Member}
import net.dv8tion.jda.api.{MessageBuilder, entities}

object CommandHelper {
  def apply(message: entities.Message): Message = new CommandHelper.Message(message)

  class Message(val _me: entities.Message) extends AnyVal {
    /** Either this message's guild, or a human-readable error */
    def guild: Either[String, Guild] = Option(_me.getGuild).toRight("You can only use this command from within a server.")

    /** Either this message's member, or a human-readable error */
    def member: Either[String, Member] =
      guild flatMap { guild =>
        Option(guild.getMember(_me.getAuthor))
          .toRight("Internal error: Can't find your server membership. This might be a temporary problem.")
      }

    /** Sanitise all mentions in the provided text for use with Discord.
      * This does not actually use the message provided in CommandHelper other
      * than to retrieve the guild for context. However, it will use the
      * message's content by default if no argument is provided.
      *
      * @param input text to sanitise
      * @return sanitised text
      */
    def mentionsToPlaintext(input: String = _me.getContentRaw): String = {
      import net.dv8tion.jda.api.entities.Message.MentionType._
      val builder = new MessageBuilder().append(input)
      guild match {
        case Right(guild) =>
          builder.stripMentions(guild, USER, ROLE, CHANNEL)
        case Left(_) =>
          builder.stripMentions(_me.getJDA, USER, ROLE, CHANNEL)
      }
      builder.getStringBuilder.toString
    }
  }
}
