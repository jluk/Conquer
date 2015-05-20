package models

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson.BSONObjectID

object JsonFormats {
  import play.api.libs.json.Json
  import play.api.data._
  import play.api.data.Forms._
  import play.modules.reactivemongo.json.BSONFormats._

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val employeeFormat = Json.format[Employee]

  implicit val format_DateTime = new Format[DateTime]{
    override def reads(json: JsValue): JsResult[DateTime] = {
      json match {
        case JsNumber(n) => JsSuccess(new DateTime(n))
        case _ => JsError("Not a number")
      }
    }

    override def writes(o: DateTime): JsValue = JsNumber(o.getMillis)
  }

  implicit val format_BSONObjectID = new Format[BSONObjectID]{
    override def reads(json: JsValue): JsResult[BSONObjectID] = {
      json match {
        case JsString(n) => JsSuccess(BSONObjectID(n))
        case _ => JsError("Not a string")
      }
    }

    override def writes(o: BSONObjectID): JsValue = JsString(o.stringify)
  }
  //implicit val userFormat = Json.format[User]

}