package uk.gov.hmrc.helloworldreactivemongo.services

import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.helloworldreactivemongo.repositories.{HelloWorld, HelloWorldRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HelloWorldService @Inject()(repo: HelloWorldRepository)(implicit ec: ExecutionContext) {

  private val logger = Logger(getClass)

  def addObjectAndCountAll(): Future[Int] =
    repo.insert(HelloWorld.random).flatMap { _ =>
      repo.count.map { count =>
        logger.info(s"count of objects = $count")
        count
      }
    }

}
