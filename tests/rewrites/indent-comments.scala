// Rewriting to indent should not remove the comments
class A /** 1 */ { /** 2 */
  def m1 = /** 3 */ { /** 4 */
    val x = if (true)
    /** 5 */ {
      "true"
    } /** 6 */
    else
    { /** 7 */
      "false"
    /** 8 */ }
/** 9 */ x
  /** 10 */ } /** 11 */
/** 12 */ }
