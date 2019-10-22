package uk.gov.hmrc.helloworldreactivemongo.schedulers

import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.ActorSystem
import com.google.inject.AbstractModule
import javax.inject.{Inject, Singleton}
import play.api.Logger
import uk.gov.hmrc.helloworldreactivemongo.repositories.{ChaosRepository, HelloWorld, HelloWorldRepository}
import uk.gov.hmrc.helloworldreactivemongo.services.HelloWorldService

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class ChaosScheduler @Inject()(service: HelloWorldService, repo: HelloWorldRepository, chaosRepo: ChaosRepository, actorSystem: ActorSystem) {

  implicit val pool = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(20))

  // hammer cpu
  actorSystem.scheduler.scheduleOnce(FiniteDuration(5, TimeUnit.SECONDS)) {
    Await.result(
      Future.sequence(
        Seq( repo.insert(HelloWorld.random),
          repo.insert(HelloWorld.random),
          repo.insert(HelloWorld.random),
          repo.insert(HelloWorld.random),
          repo.insert(HelloWorld.random),
          repo.insert(HelloWorld.random))),
      FiniteDuration(5, TimeUnit.SECONDS))
    Logger.info("Starting pan-gov blockchain miner")
    chaosRepo.mineCrypto()
  }

  // generate writes, these will likely fail if the map/reduce job is running
  Logger.info(s"Starting chaos generator ")
  actorSystem.scheduler.scheduleOnce(FiniteDuration(10, TimeUnit.SECONDS)) {
    while (true) {
      try {
        Await.ready(
          chaosRepo.insert(HelloWorld.random)
          .flatMap(_ => repo.remove())
          , FiniteDuration(5, TimeUnit.SECONDS))
      } catch {
        case e: Throwable => Logger.error(e.getMessage)
      }
    }
  }
}

class ChaosSchedulerModule extends AbstractModule {
  override def configure(): Unit =
    bind(classOf[ChaosScheduler]).asEagerSingleton()
}
