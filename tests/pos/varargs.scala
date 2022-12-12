object varargs {

  val x = if (true) 1 else 2
  
  val xs = 1 :: 2 :: Nil

  val ls = "a" :: "b" :: Nil
  
  def g(x: Int*) = x.length
  g(1, 2, 3, 4)
  g(xs*)
  g(Nil*)
  g(1)
  g()
  g(1, xs*)
  g(xs*, 1)
  g(xs*, 1, xs*, 1, xs*)
  g(xs*, 1, xs*, 1, xs*, 1)

 
  def foo[T](x: T, y: T): T = x
  foo(1, 2)

  def f(s: String*) = s.foldLeft("")(_+_)
  f("t", ls*)
  f(ls*, "t")
  f(ls*, "t", ls*, "t", ls*)
  f(ls*, "t", ls*, "t", ls*, "t")


  def l(x: Int): Unit = ()
  def l(x: Int*): Unit = ()
  l(1)
}