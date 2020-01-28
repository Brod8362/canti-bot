package score.discord.generalbot.functionality.pagination

class PaginatedStrings(data: IndexedSeq[String], linesPerPage: Int, startingPage: Int) {

  def this(data: IndexedSeq[String], linesPerPage: Int) {
    this(data, linesPerPage, 0)
  }

  val maxPages: Int = (data.length + (linesPerPage - 1)) / linesPerPage

  var currentPage: Int = startingPage.max(0).min(maxPages)

  def next(): Unit = {
    if (currentPage < maxPages-1) {
      currentPage+=1
    }
  }

  def prev(): Unit = {
    if (currentPage != 0) {
      currentPage-=1
    }
  }

  def getContentForPage(page: Int): String = {
    data.slice(page * linesPerPage, (page + 1) * linesPerPage).mkString("\n")
  }

  def getCurrentPage: String = getContentForPage(currentPage)
}
