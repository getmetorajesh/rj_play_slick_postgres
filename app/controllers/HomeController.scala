package controllers

import models._
import javax.inject._
import play.api._
import play.api.mvc._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import slick.driver.PostgresDriver.api._

import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.PostgresDriver
import slick.backend.DatabaseConfig

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject() extends Controller {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
 //  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
   
   // import dbConfig.driver.api._
    val suppliers = TableQuery[Suppliers]
  val coffees = TableQuery[Coffees]
    val dbConfig:DatabaseConfig[PostgresDriver] = DatabaseConfig.forConfig("slick.dbs.default")
  println(dbConfig.db)
  val db = dbConfig.db
  
  def index = Action {
      first()
    Ok(views.html.index("Your new application is ready."))
  }
  
 

  
  
  def first() {

    //try {

    val setup: DBIO[Unit] = DBIO.seq(
     // (suppliers.schema ++ coffees.schema).create,

      	suppliers += (101, "Acme, Inc.",      "99 Market Street", "Groundsville", "CA", "95199"),
      	suppliers += ( 49, "Superior Coffee", "1 Party Place",    "Mendocino",    "CA", "95460"),
      suppliers += (151, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966"))
    println("run")
    val setupFuture: Future[Unit] = db.run(setup)
    //setupFuture.map { x => println(x)  }

    val f = setupFuture.flatMap { _ =>

      // Insert some coffees (using JDBC's batch insert feature)
      val insertAction: DBIO[Option[Int]] = coffees ++= Seq(
        ("Colombian", 101, 7.99, 0, 0),
        ("French_Roast", 49, 8.99, 0, 0),
        ("Colombian_Decaf", 101, 8.99, 0, 0),
        ("French_Roast_Decaf", 49, 9.99, 0, 0))

      val insertAndPrintAction: DBIO[Unit] = insertAction.map { coffeesInsertResult =>
        // Print the number of rows inserted
        coffeesInsertResult foreach { numRows =>
          println(s"Inserted $numRows rows into the Coffees table")
        }
      }
      db.run(insertAndPrintAction)
    }
    Await.result(f, Duration.Inf)
    //}  finally {
    db.close
  }

}
