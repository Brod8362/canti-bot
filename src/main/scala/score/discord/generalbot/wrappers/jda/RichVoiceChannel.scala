package score.discord.generalbot.wrappers.jda

import net.dv8tion.jda.api.entities.{Member, VoiceChannel}
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction
import score.discord.generalbot.wrappers.jda.Conversions._

import scala.util.chaining._

class RichVoiceChannel(val channel: VoiceChannel) extends AnyVal {
  /** The name of this voice channel */
  def name = channel.getName

  /** A debug-friendly plaintext representation of this voice channel object */
  def unambiguousString = s"Channel(${channel.rawId} /* $name */)"

  /** The mention string for this voice channel */
  def mention = s"<#${channel.rawId}>"
}
