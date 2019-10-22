package uk.gov.hmrc.helloworldreactivemongo.repositories

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.Command
import reactivemongo.api.{BSONSerializationPack, FailoverStrategy, ReadPreference}
import reactivemongo.bson.{BSONDocument, BSONString}
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChaosRepository @Inject()(reactiveMongoComponent: ReactiveMongoComponent)(implicit val ec: ExecutionContext)
  extends ReactiveRepository(collectionName = "hello-world", reactiveMongoComponent.mongoConnector.db, HelloWorld.format) {

  private val mapJs =
    s"""
     function(a) {
       var last = hex_md5("PGBC_COIN")
       var lvl = 1;
       var coins = 0;

    while(true) {
      var target = "".pad(lvl, true, "f")
      for(var i=0;i<Number.MAX_VALUE;i++) {
          var hsh = hex_md5(last+i);
          if(hsh.substr(-lvl) ==  target) {
            print(coins +  ": pan-gov-blockchain coin found: " + hsh +  " prev: "+ last + " proof: " + i + " lvl: "  + lvl)
            emit(hsh, {'proof': i, 'prev':last, "hash": hsh});
            last = hsh
            coins+=1;
            break
          }
      }

      if(coins >= 20) {
          coins = 0;
          lvl+=1;
          print("raising level to " + lvl)
      }
    }
}
     """.stripMargin

  private val redueceJs =
    s"""
       |function(a,b) {
       |return b;
       |}
     """.stripMargin

  def mineCrypto(): Future[BSONDocument] = {
    val mrDoc = BSONDocument(
      "mapreduce" -> BSONString(collectionName),
      "map" -> BSONString(mapJs),
      "reduce" -> BSONString(redueceJs),
      "out" -> BSONString("testout")
    )
    val runner = Command.run(BSONSerializationPack, FailoverStrategy())
    Logger.info("Starting cpu stresser")
    runner.apply(this.collection.db, runner.rawCommand(mrDoc)).one(ReadPreference.primary)
  }


}
