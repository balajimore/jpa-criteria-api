package yourpackage

import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import org.h2.tools.Server

class H2Server(nodes: MockNetwork.BasketOfNodes, private val port : Int = 9092) {
  companion object {
    private val H2_NAME_RE = "^jdbc:h2:(mem:[^;:]+).*$".toRegex()
  }

  private val server: Server = org.h2.tools.Server.createTcpServer("-tcpPort", port.toString(), "-tcpAllowOthers").start()

  init {
    nodes.notaryNode.writeJDBCEndpoint()
    nodes.partyNodes.forEach { it.writeJDBCEndpoint() }
  }

  /**
   * Block the current thread so that we can connect and examine the database
   */
  fun block() {
    val lock = java.lang.Object()
    synchronized(lock) {
      lock.wait()
    }
  }

  fun stop() {
    server.stop()
  }

  private fun StartedNode<MockNetwork.MockNode>.writeJDBCEndpoint() {
    val url = this.database.dataSource.dataSourceProperties.getProperty("url")
    val databaseName = H2_NAME_RE.matchEntire(url)?.groupValues?.get(1) ?: throw RuntimeException("could not extract db name from $url")
    println("Database for ${this.info.legalIdentities.first().name.organisation} database: jdbc:h2:tcp://localhost:$port/$databaseName")
  }
}
