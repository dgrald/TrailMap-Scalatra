package org.dgrald.auth

import org.dgrald.{AnyRandom, TestDatabase}
import org.specs2.mutable.Specification

/**
  * Created by dylangrald on 10/29/16.
  */
class UserStoreSpec extends Specification {

  val database = TestDatabase.connection("test-user-db-will-be-dropped")
  val userStore = UserStore(database)

  "Should save and retrieve a user by id" in {
    val name = AnyRandom.string()
    val password = AnyRandom.string()

    val newUser = userStore.saveNewUser(name, password).get

    userStore.get(newUser.id).get must_== newUser
  }

  "Should authenticate a user" in {
    val name = AnyRandom.string()
    val password = AnyRandom.string()

    val newUser = userStore.saveNewUser(name, password)

    userStore.authenticate(name, password) must_== newUser
  }

  "Should reject the wrong password" in {
    val name = AnyRandom.string()
    val password = AnyRandom.string()

    val newUser = userStore.saveNewUser(name, password)

    userStore.authenticate(name, password + AnyRandom.string) must_== None
  }

  "Should reject a non-existent user" in {
    val name = AnyRandom.string()
    val password = AnyRandom.string()

    val newUser = userStore.saveNewUser(name, password)

    userStore.authenticate(name + AnyRandom.string(), password) must_== None
  }

  "Should not save a user if one exists with the same name" in {
    val name = AnyRandom.string()
    val password = AnyRandom.string()

    val newUser = userStore.saveNewUser(name, password)
    val secondUser = userStore.saveNewUser(name, AnyRandom.string())

    secondUser must_== None
  }
}
