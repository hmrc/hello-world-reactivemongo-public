package uk.gov.hmrc.helloworldreactivemongo.schedulers

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.helloworldreactivemongo.services.HelloWorldService

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

@Singleton
class MongoHelloWorldScheduler @Inject()(service: HelloWorldService, actorSystem: ActorSystem)(
  implicit val ec: ExecutionContext) {

  private val logger = Logger(getClass)

  val interval = FiniteDuration(10, TimeUnit.SECONDS)

  logger.info(s"Initialising update every $interval")

  actorSystem.scheduler.schedule(FiniteDuration(1, TimeUnit.SECONDS), interval) {
    logger.info("Scheduling a mongo counter..")
    service.addObjectAndCountAll().recover{case e ⇒ logger.error("failed", e)}
  }

}

class MongoHelloWorldSchedulerModule extends AbstractModule {
  override def configure(): Unit =
    bind(classOf[MongoHelloWorldScheduler]).asEagerSingleton()
}
