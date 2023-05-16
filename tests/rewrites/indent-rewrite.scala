// A collection of patterns/bugs found when rewriting the community build to indent

trait A {

  class B
// do not remove braces if empty region
class C {

}
// do not remove braces if open brace is not followed by new line
def m1(x: Int) =
{ x
.toString
  }
// add indent to pass an argument (fewer braces)
def m2: String = {
m1 {
5
}
}
// indent inner method
  def m3: Int = {
def seq = {
Seq(
"1",
"2"
)
}
seq
(1)
.toInt
}
// indent refinement
def m4: Any {
def foo: String
}
=
  new  {
    def foo: String =
    """
Hello, World!
"""
}
// indent end marker
end m4

// do not indent if closing braces not followed by new line
def m5: String = {
val x = "Hi"
x
}; def m6(x: String): String = {
s"""Bye $x ${
  x
}
do not indent in a multiline string"""
}
  def m7 = {
    val foo = ""
    val x = Seq(
      s"${foo}",
      ""
    )
  }

  // preserve indentation style before 'case'
  // but fix indentation inside 'case'
  def m9(o: Option[String]) = {
    o match
      case Some(x) => x
      case None => ""

    o match
    case Some(x) => x
    case None => ""

    o match {
    case None =>
    ""
    case Some(x) =>
    x
    }
  }

  // preserve indent of chained calls
  def m8(xs: Seq[String]) = {
    xs
      .filter {
        _ => true
      }
      .map(s => s * 2)
  }

  /** suround operators with backticks */
  object *:{
    def foo = ???
  }
  def m9 =
    5 * {
      2
    } == 10 || {
      false
    }
}
