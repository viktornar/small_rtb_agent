package small.rtb.agent

package object common {
  /** An alias for the `Any` type.
   * Denotes that the type should be filled in.
   */
  type ??? = Any

  /** An alias for the `Nothing` type.
   * Denotes that the type should be filled in.
   */
  type *** = Nothing

  /** An  union type function example with String and Int */
  def unionStringAndInt[A](a: A)(implicit ev: (Int with String) <:< A) = a match {
    case i: Int => i
    case s: String => s
  }
}
