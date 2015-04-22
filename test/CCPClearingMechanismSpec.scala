import akka.actor.{Props, ActorRef, ActorSystem}
import akka.testkit.{TestProbe, TestActorRef, TestKit}
import models._
import org.scalatest.{BeforeAndAfterAll, Matchers, GivenWhenThen, FeatureSpecLike}

import scala.util.Random


class CCPClearingMechanismSpec extends TestKit(ActorSystem("NoiseTraderSpec")) with
  FeatureSpecLike with
  GivenWhenThen with
  Matchers with
  BeforeAndAfterAll {

  /** Shutdown actor system when finished. */
  override def afterAll(): Unit = {
    system.shutdown()
  }

  def generateRandomFill(askTradingPartyRef: ActorRef,
                         bidTradingPartyRef: ActorRef,
                         maxPrice: Double = 1e6,
                         maxQuantity: Int = 10000): FillLike = {
    val instrument = Random.nextString(4)
    val price = generateRandomPrice()
    val quantity = generateRandomQuantity()

    if (Random.nextFloat() < 0.5) {
      PartialFill(askTradingPartyRef, bidTradingPartyRef, instrument, price, quantity)
    } else {
      TotalFill(askTradingPartyRef, bidTradingPartyRef, instrument, price, quantity)
    }

  }

  def generateRandomPrice(maxPrice: Double = 1000.0): Double = {
    Random.nextDouble() * maxPrice
  }

  def generateRandomQuantity(maxQuantity: Int = 10000): Int = {
    Random.nextInt(maxQuantity)
  }


  feature("CCPClearingMechanism should process transactions.") {

    val clearingMechanism = TestActorRef(Props[CCPClearingMechanism])

    scenario("CCPClearingMechanism receives a FillLike.") {

      val askTradingParty = TestProbe()
      val bidTradingParty = TestProbe()
      val fill = generateRandomFill(askTradingParty.ref, bidTradingParty.ref)

      When("CCPClearingMechanism receives a FillLike")

      clearingMechanism ! fill

      Then("AskTradingParty should receive a request for Securities")

      val securitiesRequest = RequestSecurities(fill.instrument, fill.quantity)
      askTradingParty.expectMsg(securitiesRequest)

      Then("BidTradingParty should receive a request for Payment")

      val paymentRequest = RequestPayment(fill.price * fill.quantity)
      bidTradingParty.expectMsg(paymentRequest)

      Then("CCPClearingMechanism should receive a request for Securities")

      Then("CCPClearingMechanism should receive a request for Payment")


    }

  }

}

