package org.dgrald.trails

import java.util.UUID

/**
  * Created by dylangrald on 6/23/16.
  */
case class Stash(id: String, name: String, location: Location)

object Stash {
  def apply(name: String, location: Location): Stash = {
    new Stash(UUID.randomUUID().toString, name, location)
  }
}
