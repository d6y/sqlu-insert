import slick.jdbc.PostgresProfile.api._
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Example {

  case class Export(hash: String, data: String)

  class MyTable(tag: Tag) extends Table[Export](tag, "gaexport") {
    def hash  = column[String]("hash")
    def data = column[String]("data")
    def * = (hash, data).mapTo[Export]
  }

  lazy val myTable = TableQuery[MyTable]

  def main(args: Array[String]): Unit = {

    def insert(e: Export): DBIO[Int] = 
      sqlu"""insert into gaexport values (${e.hash}, ${e.data})"""

    val program = for {
      _ <- myTable.schema.drop.asTry
      _ <- myTable.schema.create
      n <- insert(Export("hi", "ho"))
      rows <- myTable.result
    } yield (n, rows)

    val db = Database.forConfig("example")
    try 
      println(
        Await.result(db.run(program), 2.seconds)
      )
    finally db.close
  }
}
