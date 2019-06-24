package uk.gov.hmrc.helloworldreactivemongo.services

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.helloworldreactivemongo.repositories.HelloWorldRepository
import uk.gov.hmrc.mongo.{MongoConnector, MongoSpecSupport}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class HelloWorldServiceSpec
    extends WordSpec
    with Matchers
    with MongoSpecSupport
    with BeforeAndAfter
    with ScalaFutures
    with IntegrationPatience {

  before {
    dropTestCollection("hello-world")
  }

  "addObjectAndCountAll" should {
    "add one document each time it's called" in {
      val service         = new HelloWorldService(repo)
      val executionsCount = Random.nextInt(10) + 1

      val sequentialExecution =
        (1 to executionsCount).foldLeft(Future.successful(0)) { (futAcc, _) =>
          for {
            prevCount <- futAcc
            next      <- service.addObjectAndCountAll()
          } yield {
            next shouldBe prevCount + 1
            next
          }
        }

      val countOfElements = sequentialExecution.futureValue

      countOfElements shouldBe executionsCount
    }
  }

  private val reactiveMongoComponent: ReactiveMongoComponent =
    new ReactiveMongoComponent {
      override def mongoConnector: MongoConnector = mongoConnectorForTest
    }

  val repo = new HelloWorldRepository(reactiveMongoComponent)

}
