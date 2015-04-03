package models

import akka.actor.ActorRef

import scala.concurrent.duration.FiniteDuration


/** Trait representing an order for a particular security.
  *
  * The OrderLike trait should be mixed in with each specific type of order
  * (i.e., ask orders, bid orders, limit orders, market orders, etc).
  *
  */
trait OrderLike {

  /** Whether the order is buy (true) or sell (false). */
  def buy: Boolean

  /** Whether or not the order crosses some other order. */
  def crosses(other: OrderLike): Boolean

  /** Price formation rules, */
  def formPrice(other: OrderLike): Double

  /** The unique identifier of the security. */
  def instrument: String

  /** The quantity being bought or sold. */
  def quantity: Int

  /** Orders will often need to be split during the matching process. */
  def split(newQuantity: Int): OrderLike

  /** String representation of an order. */
  def toString: String

  /** The trading party for the order. */
  def tradingPartyRef: ActorRef

}
