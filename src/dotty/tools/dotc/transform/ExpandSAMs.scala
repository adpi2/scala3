package dotty.tools.dotc
package transform

import core._
import Contexts._, Symbols._, Types._, Flags._, Decorators._, StdNames._, Constants._
import SymDenotations.SymDenotation
import TreeTransforms._
import ast.untpd
import ast.Trees._

/** Expand SAM closures that cannot be represented by the JVM to anonymous classes.
 *  These fall into three categories
 *
 *   1. Partial function closures, we need to generate a isDefinedAt method for these.
 *   2. Closures implementaing non-trait classes.
 *   3. Closures that get synthesized abstract methods in the transformation pipeline. These methods can be
 *      (1) superaccessors, (2) outer references, (3) accessors for fields.
 */
class ExpandSAMs extends MiniPhaseTransform { thisTransformer =>
  override def phaseName = "expandSAMs"

  import ast.tpd._

  def noJvmSam(cls: ClassSymbol)(implicit ctx: Context): Boolean =
    !cls.is(Trait) || ExplicitOuter.needsOuterIfReferenced(cls) || cls.typeRef.fields.nonEmpty

  override def transformBlock(tree: Block)(implicit ctx: Context, info: TransformerInfo): Tree = tree match {
    case Block(stats @ (fn: DefDef) :: Nil, Closure(_, fnRef, tpt)) if fnRef.symbol == fn.symbol =>
      tpt.tpe match {
        case NoType => tree // it's a plain function
        case tpe @ SAMType(_) if !noJvmSam(tpe.classSymbol.asClass) =>
          if (tpe isRef defn.PartialFunctionClass) toPartialFunction(tree)
          else tree
        case tpe =>
          cpy.Block(tree)(stats,
              AnonClass(tpe, fn.symbol.asTerm :: Nil, nme.apply :: Nil))
      }
    case _ =>
      tree
  }

  private def toPartialFunction(tree: Block)(implicit ctx: Context, info: TransformerInfo): Tree = {
    val Block(
          (applyDef @ DefDef(nme.ANON_FUN, Nil, List(List(param)), _, _)) :: Nil,
          Closure(_, _, tpt)) = tree
    val applyRhs: Tree = applyDef.rhs
    val applyFn = applyDef.symbol.asTerm

    val MethodType(paramNames, paramTypes) = applyFn.info
    val isDefinedAtFn = applyFn.copy(
        name  = nme.isDefinedAtImpl,
        flags = Synthetic | Method,
        info = MethodType(paramNames, paramTypes, defn.BooleanType)).asTerm
    val tru = Literal(Constant(true))
    def isDefinedAtRhs(paramRefss: List[List[Tree]]) = applyRhs match {
      case Match(selector, cases) =>
        assert(selector.symbol == param.symbol)
        val paramRef = paramRefss.head.head
        // Again, the alternative
        //     val List(List(paramRef)) = paramRefs
        // fails with a similar self instantiation error
        def translateCase(cdef: CaseDef): CaseDef =
          cpy.CaseDef(cdef)(body = tru).changeOwner(applyFn, isDefinedAtFn)
        val defaultSym = ctx.newSymbol(isDefinedAtFn, nme.WILDCARD, Synthetic, selector.tpe.widen)
        val defaultCase =
          CaseDef(
            Bind(defaultSym, untpd.Ident(nme.WILDCARD).withType(selector.tpe.widen)),
            EmptyTree,
            Literal(Constant(false)))
        cpy.Match(applyRhs)(paramRef, cases.map(translateCase) :+ defaultCase)
      case _ =>
        tru
    }
    val isDefinedAtDef = transformFollowingDeep(DefDef(isDefinedAtFn, isDefinedAtRhs(_)))
    val anonCls = AnonClass(tpt.tpe, List(applyFn, isDefinedAtFn), List(nme.apply, nme.isDefinedAt))
    cpy.Block(tree)(List(applyDef, isDefinedAtDef), anonCls)
  }
}
