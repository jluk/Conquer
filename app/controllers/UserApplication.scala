//package controllers
//
//import java.util.concurrent.TimeoutException
//
//import reactivemongo.core.commands.LastError
//
//import scala.concurrent.Future
//import scala.concurrent.duration.DurationInt
//
//import models.User
//import models.JsonFormats.userFormat
//import models.Page
//import play.api.Logger
//import play.api.data.Form
//import play.api.data.Forms.date
//import play.api.data.Forms.ignored
//import play.api.data.Forms.mapping
//import play.api.data.Forms.nonEmptyText
//import play.api.libs.concurrent.Execution.Implicits.defaultContext
//import play.api.libs.json.Json
//import play.api.libs.json.Json.toJsFieldJsValueWrapper
//import play.api.mvc.{Result, Action, Controller}
//import play.modules.reactivemongo.MongoController
//import play.modules.reactivemongo.json.collection.JSONCollection
//import reactivemongo.bson.BSONObjectID
//import views.html
//
///**
// * Created by justinluk1 on 5/15/15.
// */
//object UserApplication extends Controller with MongoController {
//
//    implicit val timeout = 10.seconds
//
//    /**
//     * Describe the user form
//     *
//     *   _id: BSONObjectID,
//     *  username: String,
//     *  passwordDigest: String
//     */
//    val userForm = Form(
//      mapping(
//        "id" -> ignored(BSONObjectID.generate: BSONObjectID),
//        "username" -> nonEmptyText,
//        "passworddigest" -> nonEmptyText)(User.apply)(User.unapply))
//
//    /*
//     * Get a JSONCollection (a Collection implementation that is designed to work
//     * with JsObject, Reads and Writes.)
//     * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
//     * the collection reference to avoid potential problems in development with
//     * Play hot-reloading.
//     */
//    def collection: JSONCollection = db.collection[JSONCollection]("users")
//
//    // ------------------------------------------ //
//    // Using case classes + Json Writes and Reads //
//    // ------------------------------------------ //
//    import play.api.data.Form
//    import models._
//    import models.JsonFormats._
//
//    /**
//     * Handle default path requests, redirect to User list
//     */
//    def index = Action { Home }
//
//    /**
//     * This result directly redirect to the application home.
//     */
//    val Home = Redirect(routes.Application.list())
//
////    /**
////     * Display the paginated list of Users.
////     *
////     * @param page Current page number (starts from 0)
////     * @param orderBy Column to be sorted
////     * @param filter Filter applied on User names
////     */
////    def list(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
////      for {
////        users <- if (filter.length > 0) {
////          collection.find(Json.obj("name" -> filter)).cursor[User].collect[List]()
////        } else {
////          collection.genericQueryBuilder.cursor[User].collect[List]()
////        }
////      } yield {
////        Ok(html.list(Page(users, 0, 10, 20), orderBy, filter))
////      }
////    }
//
////    /**
////     * Display the 'edit form' of a existing user.
////     *
////     * @param id Id of the user to edit
////     */
////    def edit(id: String) = Action.async {
////      for {
////        users <- collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[User].collect[List]()
////      } yield {
////        Ok(html.editForm(id, userForm.fill(users.head)))
////      }
////    }
////
////    /**
////     * Handle the 'edit form' submission
////     *
////     * @param id Id of the user to edit
////     */
////    def update(id: String) = Action.async { implicit request =>
////      userForm.bindFromRequest.fold(
////        formWithErrors => Future.successful(BadRequest(html.editForm(id, formWithErrors))),
////        user => {
////          for {
////            lastError <- collection.update(Json.obj("_id" -> Json.obj("$oid" -> id)), user.copy(_id = BSONObjectID(id)))
////          } yield {
////            flashResult(lastError, s"User ${user.username} has been updated")
////          }
////        })
////    }
//
//    /**
//     * Display the 'new user form'.
//     */
//    def create = Action {
//      Ok(html.createUser(userForm))
//    }
//
//    /**
//     * Handle the 'new user form' submission.
//     */
//    def save = Action.async { implicit request =>
//      userForm.bindFromRequest.fold(
//        formWithErrors => Future.successful(BadRequest(html.createUser(formWithErrors))),
//        user => {
//          for {
//            lastError <- collection.insert(user.copy(_id = BSONObjectID.generate))
//          } yield {
//            flashResult(lastError, s"User ${user.username} has been created")
//          }
//        })
//    }
//
//    /**
//     * Helper method to check for lastError success or failure
//     *
//     * @param lastError LastError to be checked for success
//     * @param success String to be flashed upon success
//     * @return Result
//     */
//    def flashResult(lastError: LastError, success: String): Result = {
//      if (lastError.ok) {
//        Home.flashing("success" -> success)
//      } else {
//        Home.flashing("failure" -> "Failed to write to mongo!")
//      }
//    }
//}
