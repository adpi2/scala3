sealed trait P
case class PC1(a: String) extends P
case class PC2(b: Int) extends P

def Test = MatchTest.test(PC2(10): P)
