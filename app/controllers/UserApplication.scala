package controllers

import java.util.concurrent.TimeoutException

import play.Application
import reactivemongo.core.commands.LastError

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import models.User
import models.JsonFormats.userFormat
import models.Page
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms.date
import play.api.data.Forms.ignored
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.mvc.{Result, Action, Controller}
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import views.html

/**
 * Created by justinluk1 on 5/15/15.
 */
object UserApplication extends Controller with MongoController {

    implicit val timeout = 10.seconds

    /**
     * Describe the user form
     *
     *   _id: BSONObjectID,
     *  username: String,
     *  passwordDigest: String
     */
    val userForm = Form(
      mapping(
        "id" -> ignored(BSONObjectID.generate: BSONObjectID),
        "username" -> nonEmptyText,
        "passworddigest" -> nonEmptyText)(User.apply)(User.unapply))

    /*
     * Get a JSONCollection (a Collection implementation that is designed to work
     * with JsObject, Reads and Writes.)
     * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
     * the collection reference to avoid potential problems in development with
     * Play hot-reloading.
     */
    def collection: JSONCollection = db.collection[JSONCollection]("users")

    // ------------------------------------------ //
    // Using case classes + Json Writes and Reads //
    // ------------------------------------------ //
    import play.api.data.Form
    import models._
    import models.JsonFormats._

    /**
     * Handle default path requests, redirect to User list
     */
    def index = Action { Home }

    /**
     * This result directly redirect to the application home.
     */
    val Home = Redirect(routes.Application.list())

    /**
     * Display the 'new user form'.
     */
    def create = Action {
      Ok(html.createUser(userForm))
    }

    /**
     * Handle the 'new user form' submission.
     */
    def save = Action.async { implicit request =>
      userForm.bindFromRequest.fold(
        formWithErrors => Future.successful(BadRequest(html.createUser(formWithErrors))),
        user => {
          for {
            lastError <- collection.insert(user.copy(_id = BSONObjectID.generate))
          } yield {
            flashResult(lastError, s"User ${user.username} has been created")
          }
        })
    }

}
