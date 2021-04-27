package lila.relay

import org.joda.time.DateTime
import reactivemongo.api.bson._
import reactivemongo.akkastream.{ cursorProducer, AkkaStreamCursor }
import reactivemongo.api.ReadPreference

import lila.db.dsl._

final private class RelayRoundRepo(val coll: Coll)(implicit ec: scala.concurrent.ExecutionContext) {

  import BSONHandlers._

  def lookup(local: String, foreign: String) = $lookup.simple(coll, "tour", local, foreign)

  // def scheduled =
  //   coll
  //     .find($doc(selectors scheduled true))
  //     .sort($sort asc "startsAt")
  //     .cursor[Relay]()
  //     .list()

  // def ongoing =
  //   coll
  //     .find($doc(selectors ongoing true))
  //     .sort($sort asc "startedAt")
  //     .cursor[Relay]()
  //     .list()

  // private[relay] def officialCursor(batchSize: Int): AkkaStreamCursor[Relay] =
  //   coll
  //     .find(selectors officialOption true)
  //     .sort($sort desc "startsAt")
  //     .batchSize(batchSize)
  //     .cursor[Relay](ReadPreference.secondaryPreferred)

  def byTourOrdered(tour: RelayTour): Fu[List[RelayRound]] =
    coll
      .find(selectors.tour(tour.id))
      .sort(chronoSort)
      .cursor[RelayRound]()
      .list(RelayTour.maxRelays)

  val chronoSort = $doc("createdAt" -> 1)

  val reverseChronoSort = $doc("createdAt" -> -1)

  private[relay] object selectors {
    def tour(id: RelayTour.Id) = $doc("tourId" -> id)
    // def scheduled(official: Boolean) =
    //   officialOption(official) ++ $doc(
    //     "startsAt" $gt DateTime.now.minusHours(1),
    //     "startedAt" $exists false
    //   )
    // def ongoing(official: Boolean) =
    //   officialOption(official) ++ $doc(
    //     "startedAt" $exists true,
    //     "finished" -> false
    //   )
    def finished(official: Boolean) =
      // officialOption(official) ++
      $doc(
        "startedAt" $exists true,
        "finished" -> true
      )
  }
}
