import javax.servlet.ServletContext

import org.dgrald.auth.UserStore
import org.dgrald.trails._
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    implicit val userStore = UserStore()
    context.mount(new StashMap(StashStore(), JsonConverter()), "/*")
  }
}