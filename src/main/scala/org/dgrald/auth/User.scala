package org.dgrald.auth

/**
  * Created by dylangrald on 10/28/16.
  */
case class User(id: String, name: String)

object User {

  val userStore = UserStore()

  def apply(id: String): User = {
    userStore.get(id) match {
      case Some(user) => user
      case None => throw new Exception("Not found")
    }
  }

}
