package models

import play.api.libs.json._
import reactivemongo.bson._
import java.util.Date
import org.joda.time.DateTime
import JsonFormats._


case class Employee(
  _id: BSONObjectID,
  name: String,
  address: String,
  dob: Date,
  joiningDate: Date,
  designation: String
)

case class User(
  _id: BSONObjectID,
  username: String,
  passwordDigest: String
)

object User{
  implicit val format_User = Json.format[User]
}

//case class Stat(
//  _id: BSONObjectID,
//  userId: BSONObjectID,
//  name: String,
//  value: String
//)

case class WorkoutPlan(
  _id: BSONObjectID,
  name: String,
  exercises: Seq[String]
)

object WorkoutPlan{
  implicit val format_WorkoutPlan = Json.format[WorkoutPlan]
}

case class Workout(
  _id: BSONObjectID,
  when: DateTime,
  userId: BSONObjectID,
  workoutPlanId: BSONObjectID,
  weight: Float,
  exerciseToWeightLifted: Map[String, Seq[Int]]
)

object Workout{
  implicit val format_Workout = Json.format[Workout]
}

case class TrackData(
  userId: BSONObjectID,
  username: String,
  userWeight: Option[Float], //Stat,
  allWorkoutPlans: Seq[WorkoutPlan],
  exerciseLiftHistory: Map[String, Seq[Int]]
)

/**
 * Helper for pagination.
 */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

