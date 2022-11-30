object varargs {
  // List(1, 2, 3)
  def g(x: Int*) = x.length

  def f(s: String*) = s.foldLeft("")(_+_)
  // g(1, 2, 3, 4)
  // val x = if (true) 1 else 2
  // def foo[T](x: T, y: T): T = x
  // foo(1, 2)
  val xs = 1 :: 2 :: Nil
  val ls = "a" :: "b" :: Nil
  
  // g(xs*)
  // g(Nil*)
  // g(1)
  // g()
  g(1, xs*)
  g(xs*, 1)
  g(xs*, 1, xs*, 1, xs*)
  g(xs*, 1, xs*, 1, xs*, 1)

  f("t", ls*)
  f(ls*, "t")
  f(ls*, "t", ls*, "t", ls*)
  f(ls*, "t", ls*, "t", ls*, "t")


  // def f(x: Int): Unit = ()
  // def f(x: Int*): Unit = ()
  // f(1)
}