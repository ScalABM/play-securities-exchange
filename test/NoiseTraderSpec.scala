import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import models._
import org.scalatest.{BeforeAndAfterAll, FeatureSpecLike, GivenWhenThen, Matchers}

import scala.collection.mutable
import scala.util.Random


class NoiseTraderSpec extends TestKit(ActorSystem("NoiseTraderSpec")) with
  FeatureSpecLike with
  GivenWhenThen with
  Matchers with
  BeforeAndAfterAll {

  /** Shutdown actor system when finished. */
  override def afterAll(): Unit = {
    system.shutdown()
  }

  /* Create an instance of a Market actor. */
  val testInstrument = "AAPL"

  val marketRef = TestActorRef(new DoubleAuctionMechanism(testInstrument))

  val market = marketRef.underlyingActor

  /* Create an instance of a NoiseTrader for testing. */
  val assets = mutable.Map[String, Int]((testInstrument, 0))

  val cash = Double.PositiveInfinity

  val prng = new Random()

  val noiseTraderRef = TestActorRef(new NoiseTrader(assets, cash, marketRef, prng))

  val noiseTrader = noiseTraderRef.underlyingActor

  feature("NoiseTrader should be able to generate new orders.") {

    scenario("NoiseTrader wants to generate a new ask order") {

      When("NoiseTrader specifies its desired ask price")

      val askPrice = noiseTrader.decideAskPrice()

      Then("the desired ask price should be strictly positive (and less than some upper bound)")

      askPrice should (be > 0.0 and be < noiseTrader.maxPrice)

      When("NoiseTrader specifies its desired ask quantity")

      val askQuantity = noiseTrader.decideAskQuantity()

      Then("the desired ask quantity should be strictly positive (and less than some upper bound)")

      askQuantity should (be >= 1 and be <= noiseTrader.maxQuantity)

    }

    scenario("NoiseTrader wants to generate a new bid order") {

      When("NoiseTrader specifies its desired bid price.")

      val bidPrice = noiseTrader.decideBidPrice()

      Then("the desired bid price should be strictly positive (and less than some upper bound).")

      bidPrice should (be > 0.0 and be < noiseTrader.maxPrice)

      When("NoiseTrader specifies its desired bid quantity")

      val bidQuantity = noiseTrader.decideBidQuantity()

      Then("the desired bid quantity should be strictly positive (and less than some upper bound).")

      bidQuantity should (be >= 1 and be <= noiseTrader.maxQuantity)

    }

    scenario("NoiseTrader wants to generate a new order") {

      When("NoiseTrader specifies an instrument to trade")

      val instrument = noiseTrader.decideInstrument()

      Then("the desired instrument should be feasible")

      noiseTrader.assets.keySet should contain(instrument)

      When("NoiseTrader generates a new order")

      val order = noiseTrader.generateNewOrder()

      Then("the generated order has type OrderLike.")

      assert(order.isInstanceOf[OrderLike])

    }

  }

}