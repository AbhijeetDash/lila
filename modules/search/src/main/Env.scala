package lila.search

import com.softwaremill.macwire.*
import play.api.Configuration
import play.api.libs.ws.StandaloneWSClient
import lila.search.client.SearchClient

import lila.common.autoconfig.*

@Module
private class SearchConfig(
    val enabled: Boolean,
    val writeable: Boolean,
    val endpoint: String
)

@Module
final class Env(
    appConfig: Configuration,
    ws: StandaloneWSClient
)(using Executor):

  private val config = appConfig.get[SearchConfig]("search")(AutoConfig.loader)

  private def makeHttp(index: Index): ESClientHttp = wire[ESClientHttp]

  val client = if config.enabled then SearchClient.play(ws, config.endpoint) else SearchClient.noop
