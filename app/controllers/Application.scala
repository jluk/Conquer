package controllers

import java.util.concurrent.TimeoutException

import reactivemongo.core.commands.LastError

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

import models.Employee
import models.JsonFormats.employeeFormat
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

/*
 * Example using ReactiveMongo + Play JSON library.
 *
 * There are two approaches demonstrated in this controller:
 * - using JsObjects directly
 * - using case classes that can be turned into Json using Reads and Writes.
 *
 * This controller uses case classes and their associated Reads/Writes
 * to read or write JSON structures.
 *
 * Instead of using the default Collection implementation (which interacts with
 * BSON structures + BSONReader/BSONWriter), we use a specialized
 * implementation that works with JsObject + Reads/Writes.
 *
 * Of course, you can still use the default Collection implementation
 * (BSONCollection.) See ReactiveMongo examples to learn how to use it.
 */

object Application extends Controller with MongoController {

  implicit val timeout = 10.seconds

  /**
   * Describe the employee form (used in both edit and create screens).
   */
  val employeeForm = Form(
    mapping(
      "id" -> ignored(BSONObjectID.generate: BSONObjectID),
//      "date" -> date("yyyy,MM-dd"),
      "name" -> nonEmptyText,
      "address" -> nonEmptyText,
      "dob" -> date("yyyy-MM-dd"),
      "joiningDate" -> date("yyyy-MM-dd"),
      "designation" -> nonEmptyText)(Employee.apply)(Employee.unapply))

  /*
   * Get a JSONCollection (a Collection implementation that is designed to work
   * with JsObject, Reads and Writes.)
   * Note that the `collection` is not a `val`, but a `def`. We do _not_ store
   * the collection reference to avoid potential problems in development with
   * Play hot-reloading.
   */
  def collection: JSONCollection = db.collection[JSONCollection]("employees")

  // ------------------------------------------ //
  // Using case classes + Json Writes and Reads //
  // ------------------------------------------ //
  import play.api.data.Form
  import models._
  import models.JsonFormats._

  /**
   * Handle default path requests, redirect to employee list
   */
  def index = Action { Home }

  /**
   * This result directly redirect to the application home.
   */
  val Home = Redirect(routes.Application.list())

  /**
   * Display the paginated list of employees.
   *
   * @param page Current page number (starts from 0)
   * @param orderBy Column to be sorted
   * @param filter Filter applied on employee names
   */
  def list(page: Int, orderBy: Int, filter: String) = Action.async { implicit request =>
    for {
      employees <- if (filter.length > 0) {
        collection.find(Json.obj("name" -> filter)).cursor[Employee].collect[List]()
      } else {
        collection.genericQueryBuilder.cursor[Employee].collect[List]()
      }
    } yield {
      Ok(html.list(Page(employees, 0, 10, 20), orderBy, filter))
    }
  }

  /**
   * Display the 'edit form' of a existing Employee.
   *
   * @param id Id of the employee to edit
   */
  def edit(id: String) = Action.async {
    for {
      employees <- collection.find(Json.obj("_id" -> Json.obj("$oid" -> id))).cursor[Employee].collect[List]()
    } yield {
      Ok(html.editForm(id, employeeForm.fill(employees.head)))
    }
  }

  /**
   * Handle the 'edit form' submission
   *
   * @param id Id of the employee to edit
   */
  def update(id: String) = Action.async { implicit request =>
    employeeForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.editForm(id, formWithErrors))),
      employee => {
        for {
          lastError <- collection.update(Json.obj("_id" -> Json.obj("$oid" -> id)), employee.copy(_id = BSONObjectID(id)))
        } yield {
          flashResult(lastError, s"Employee ${employee.name} has been updated")
        }
      })
  }

  /**
   * Display the 'new employee form'.
   */
  def create = Action {
    Ok(html.createForm(employeeForm))
  }

  /**
   * Handle the 'new employee form' submission.
   */
  def save = Action.async { implicit request =>
    employeeForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(html.createForm(formWithErrors))),
      employee => {
        for {
          lastError <- collection.insert(employee.copy(_id = BSONObjectID.generate))
        } yield {
          flashResult(lastError, s"Employee ${employee.name} has been created")
        }
      })
  }

  /**
   * Handle employee deletion.
   */
  def delete(id: String) = Action.async {
    for {
      lastError <- collection.remove(Json.obj("_id" -> Json.obj("$oid" -> id)), firstMatchOnly = true)
    } yield {
      flashResult(lastError, "Employee has been deleted")
    }
  }

  /**
   * Helper method to check for lastError success or failure
   *
   * @param lastError LastError to be checked for success
   * @param success String to be flashed upon success
   * @return Result
   */
  def flashResult(lastError: LastError, success: String): Result = {
    if (lastError.ok) {
      Home.flashing("success" -> success)
    } else {
      Home.flashing("failure" -> "Failed to write to mongo!")
    }
  }

}