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

  // preserve indent of chained calls
  def m8(xs: Seq[String]) = {
    xs
      .filter {
        _ => true
      }
      .map(s => s * 2)
  }

  // do not remove braces inside (...) or [...]
  // remove braces after =>
  def m9(xs: List[Int]) = {
    println(
      xs.size match {
        case 1 =>
          xs match {
            case 1 :: Nil => "1"
            case _ => s"${xs.head} :: Nil"
          }
        case _ => {
          "xs"
        }
      }
    )
    println(
      if (xs.size > 0) {
        "foo"
      } else {
        "bar"
      }
    )
    xs.map(
      x => {
        x
      }
    ).map {
      x => {
        x
      }
    }
  }
  import reflect.Selectable.reflectiveSelectable
  def m10(xs: List[
    Any {
      def foo: String
    }
  ]) =
    xs.map(x => x.foo)

  // preserve indentation style before 'case'
  // but fix indentation inside 'case'
  def m11(o: Option[String]) = {
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

  /** suround operators with backticks */
  object *:{
    def foo = ???
  }
  def m12 =
    5 * {
      2
    } == 10 || {
      false
    }
}
