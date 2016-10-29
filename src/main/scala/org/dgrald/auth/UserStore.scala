package org.dgrald.auth

import java.security.SecureRandom
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import com.mongodb.casbah.Imports._
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64
import org.dgrald.Database

/**
  * Created by dylangrald on 10/28/16.
  */
trait UserStore {
  def authenticate(name: String, password: String): Option[User]

  def get(id: String): Option[User]

  def saveNewUser(username: String, password: String): Option[User]
}

object UserStore {
  def apply(): UserStore = UserStore(Database.connection)

  def apply(mongoDB: MongoDB): UserStore = {
    new UserStoreImplementation(mongoDB)
  }
}

private class UserStoreImplementation(database: MongoDB) extends UserStore {

  val userCollection = database("user")

  override def authenticate(name: String, password: String): Option[User] = {
    userCollection.findOne(MongoDBObject("name" -> name)) match {
      case Some(user) =>
        val salt = user.getAs[String]("pass_salt").get
        val (hash, _) = encrypt(password, Base64.decode(salt))
        val correctPass = user.getAs[String]("pass_hash").get
        if(hash.equals(correctPass)) {
          Some(User(user.getAs[String]("_id").get, user.getAs[String]("name").get))
        } else {
          None
        }
      case None => None
    }
  }

  override def get(id: String): Option[User] = {
    val userIdMatch = userCollection.findOneByID(id)
    userIdMatch match {
      case Some(userObject) =>
        val id = userObject.getAs[String]("_id").get
        val name = userObject.getAs[String]("name").get

        Some(User(id, name))
      case None => None
    }
  }

  override def saveNewUser(username: String, password: String): Option[User] = {
    userCollection.findOne(MongoDBObject("name" -> username)) match {
      case Some(_) => None
      case None =>
        val id = UUID.randomUUID().toString
        val newUser = User(id, username)

        val salt = new SecureRandom().generateSeed(20)
        val (passHash, saltString) = encrypt(password, salt)

        val userDbObject = MongoDBObject("_id" -> id, "name" -> username, "pass_hash" -> passHash, "pass_salt" -> saltString)
        userCollection.insert(userDbObject)

        Some(newUser)
    }
  }

  private def encrypt(password: String, salt: Array[Byte]): (String, String) = {
    val spec = new PBEKeySpec(password.toCharArray, salt, 2048, 160)
    val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
    val hash = f.generateSecret(spec).getEncoded

    val passHash = Base64.encode(hash)
    val saltString = Base64.encode(salt)
    (passHash, saltString)
  }
}