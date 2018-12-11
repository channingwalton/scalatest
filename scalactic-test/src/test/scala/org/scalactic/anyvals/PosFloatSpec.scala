/*
 * Copyright 2001-2014 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalactic.anyvals

import org.scalatest._
import OptionValues._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.prop.PropertyChecks
// SKIP-SCALATESTJS,NATIVE-START
import scala.collection.immutable.NumericRange
// SKIP-SCALATESTJS,NATIVE-END
import scala.util.{Failure, Success, Try}
import org.scalactic.{Good, Bad}
import org.scalactic.{Pass, Fail}
import org.scalactic.Equality
import org.scalactic.NumberCompatHelper

trait PosFloatSpecSupport {

  implicit def tryEquality[T]: Equality[Try[T]] = new Equality[Try[T]] {
    override def areEqual(a: Try[T], b: Any): Boolean = a match {
      // I needed this because with GenDrivenPropertyChecks, got:
      // [info] - should offer a '%' method that is consistent with Int *** FAILED ***
      // [info]   Success(NaN) did not equal Success(NaN) (PosIntExperiment.scala:498)
      case Success(float: Float) if float.isNaN =>
        b match {
          case Success(bFloat: Float) if bFloat.isNaN => true
          case _ => false
        }
      case Success(double: Double) if double.isNaN => 
        b match {
          case Success(bDouble: Double) if bDouble.isNaN => true
          case _ => false
        }
      case _: Success[_] => a == b
      case Failure(ex) => b match {
        case _: Success[_] => false
        case Failure(otherEx) => ex.getClass == otherEx.getClass && ex.getMessage == otherEx.getMessage
        case _ => false
      }
    }
  }
}

class PosFloatSpec extends FunSpec with Matchers with PropertyChecks with TypeCheckedTripleEquals with PosFloatSpecSupport {

  describe("A PosFloat") {
    describe("should offer a from factory method that") {
      it("returns Some[PosFloat] if the passed Float is greater than 0") {
        PosFloat.from(50.23F).value.value shouldBe 50.23F
        PosFloat.from(100.0F).value.value shouldBe 100.0F
      }
      it("returns None if the passed Float is NOT greater than 0") {
        PosFloat.from(0.0F) shouldBe None
        PosFloat.from(-0.00001F) shouldBe None
        PosFloat.from(-99.9F) shouldBe None
      }
    } 
    describe("should offer an ensuringValid factory method that") {
      it("returns PosFloat if the passed Float is greater than 0") {
        PosFloat.ensuringValid(50.23F).value shouldBe 50.23F
        PosFloat.ensuringValid(100.0F).value shouldBe 100.0F
        PosFloat.ensuringValid(Float.PositiveInfinity).value shouldBe Float.PositiveInfinity
      }
      it("throws AssertionError if the passed Float is NOT greater than 0") {
        an [AssertionError] should be thrownBy PosFloat.ensuringValid(0.0F)
        an [AssertionError] should be thrownBy PosFloat.ensuringValid(-0.00001F)
        an [AssertionError] should be thrownBy PosFloat.ensuringValid(-99.9F)
        an [AssertionError] should be thrownBy PosFloat.ensuringValid(Float.NegativeInfinity)
        an [AssertionError] should be thrownBy PosFloat.ensuringValid(Float.NaN)
      }
    }
    describe("should offer a tryingValid factory method that") {
      import TryValues._
      it("returns a PosFloat wrapped in a Success if the passed PosFloat is greater than 0") {
        PosFloat.tryingValid(50.3f).success.value.value shouldBe 50.3f
        PosFloat.tryingValid(100.0f).success.value.value shouldBe 100.0f
      }

      it("returns an AssertionError wrapped in a Failure if the passed Float is NOT greater than 0") {
        PosFloat.tryingValid(0.0f).failure.exception shouldBe an [AssertionError]
        PosFloat.tryingValid(-1.0f).failure.exception shouldBe an [AssertionError]
        PosFloat.tryingValid(-99.9f).failure.exception shouldBe an [AssertionError]
      }
    }
    describe("should offer a passOrElse factory method that") {
      it("returns a Pass if the given Float is greater than 0") {
        PosFloat.passOrElse(50.0f)(i => i) shouldBe Pass
        PosFloat.passOrElse(100.0f)(i => i) shouldBe Pass
      }
      it("returns an error value produced by passing the given Float to the given function if the passed Float is NOT greater than 0, wrapped in a Fail") {
        PosFloat.passOrElse(0.0f)(i => s"$i did not taste good") shouldBe Fail(0.0f + " did not taste good")
        PosFloat.passOrElse(-1.1f)(i => i) shouldBe Fail(-1.1f)
        PosFloat.passOrElse(-99.0f)(i => i + 3.0f) shouldBe Fail(-96.0f)
      }
    }
    describe("should offer a goodOrElse factory method that") {
      it("returns a PosFloat wrapped in a Good if the given Float is greater than 0") {
        PosFloat.goodOrElse(50.3f)(i => i) shouldBe Good(PosFloat(50.3f))
        PosFloat.goodOrElse(100.0f)(i => i) shouldBe Good(PosFloat(100.0f))
      }
      it("returns an error value produced by passing the given Float to the given function if the passed Float is NOT greater than 0, wrapped in a Bad") {
        PosFloat.goodOrElse(0.0f)(i => s"$i did not taste good") shouldBe Bad(0.0f + " did not taste good")
        PosFloat.goodOrElse(-1.1f)(i => i) shouldBe Bad(-1.1f)
        PosFloat.goodOrElse(-99.0f)(i => i + 3.0f) shouldBe Bad(-96.0f)
      }
    }
    describe("should offer a rightOrElse factory method that") {
      it("returns a PosFloat wrapped in a Right if the given Float is greater than 0") {
        PosFloat.rightOrElse(50.3f)(i => i) shouldBe Right(PosFloat(50.3f))
        PosFloat.rightOrElse(100.0f)(i => i) shouldBe Right(PosFloat(100.0f))
      }
      it("returns an error value produced by passing the given Float to the given function if the passed Float is NOT greater than 0, wrapped in a Left") {
        PosFloat.rightOrElse(0.0f)(i => s"$i did not taste good") shouldBe Left(0.0f + " did not taste good")
        PosFloat.rightOrElse(-1.1f)(i => i) shouldBe Left(-1.1f)
        PosFloat.rightOrElse(-99.9f)(i => i + 3.0f) shouldBe Left(-96.9f)
      }
    }
    describe("should offer an isValid predicate method that") {
      it("returns true if the passed Float is greater than 0") {
        PosFloat.isValid(50.23f) shouldBe true
        PosFloat.isValid(100.0f) shouldBe true
        PosFloat.isValid(0.0f) shouldBe false
        PosFloat.isValid(-0.0f) shouldBe false
        PosFloat.isValid(-0.00001f) shouldBe false
        PosFloat.isValid(-99.9f) shouldBe false
      }
    } 
    describe("should offer a fromOrElse factory method that") {
      it("returns a PosFloat if the passed Float is greater than 0") {
        PosFloat.fromOrElse(50.23f, PosFloat(42.0f)).value shouldBe 50.23f
        PosFloat.fromOrElse(100.0f, PosFloat(42.0f)).value shouldBe 100.0f
      }
      it("returns a given default if the passed Float is NOT greater than 0") {
        PosFloat.fromOrElse(0.0f, PosFloat(42.0f)).value shouldBe 42.0f
        PosFloat.fromOrElse(-0.00001f, PosFloat(42.0f)).value shouldBe 42.0f
        PosFloat.fromOrElse(-99.9f, PosFloat(42.0f)).value shouldBe 42.0f
      }
    } 
    it("should offer MaxValue and MinValue factory methods") {
      PosFloat.MaxValue shouldEqual PosFloat.from(Float.MaxValue).get
      PosFloat.MinValue shouldEqual
        PosFloat.from(Float.MinPositiveValue).get
    }
    it("should offer a PositiveInfinity factory method") {
      PosFloat.PositiveInfinity shouldEqual PosFloat.ensuringValid(Float.PositiveInfinity)
    }
    it("should offer a NegativeInfinity factory method") {
      "PosFloat.NegativeInfinity" shouldNot compile
    }
    it("should offer a isPosInfinity method that returns true if the instance is PositiveInfinity") {
      PosFloat.ensuringValid(Float.PositiveInfinity).isPosInfinity shouldBe true
      PosFloat(1.0f).isPosInfinity shouldBe false
    }
    it("should not offer a isNegInfinity method") {
      "PosFloat(1.0f).isNegInfinity" shouldNot compile
    }

    it("should be sortable") {
      val xs = List(PosFloat(2.2F), PosFloat(4.4F), PosFloat(1.1F),
                    PosFloat(3.3F))
      xs.sorted shouldEqual List(PosFloat(1.1F), PosFloat(2.2F), PosFloat(3.3F),
                                 PosFloat(4.4F))
    }

    describe("when created with apply method") {
  
      it("should compile when 8 is passed in") {
        "PosFloat(8)" should compile
        PosFloat(8).value shouldEqual 8.0F
        "PosFloat(8L)" should compile
        PosFloat(8L).value shouldEqual 8.0F
        "PosFloat(8.0F)" should compile
        PosFloat(8.0F).value shouldEqual 8.0F
      }
  
      it("should not compile when 0 is passed in") {
        "PosFloat(0)" shouldNot compile
        "PosFloat(0L)" shouldNot compile
        "PosFloat(0.0F)" shouldNot compile
      }
  
      it("should not compile when -8 is passed in") {
        "PosFloat(-8)" shouldNot compile
        "PosFloat(-8L)" shouldNot compile
        "PosFloat(-8.0F)" shouldNot compile
      }
      it("should not compile when x is passed in") {
        val a: Int = -8
        "PosFloat(a)" shouldNot compile
        val b: Long = -8L
        "PosFloat(b)" shouldNot compile
        val c: Float = -8.0F
        "PosFloat(c)" shouldNot compile
      }
    }
    describe("when specified as a plain-old Float") {

      def takesPosFloat(pos: PosFloat): Float = pos.value

      it("should compile when 8 is passed in") {
        "takesPosFloat(8)" should compile
        takesPosFloat(8) shouldEqual 8.0F
        "takesPosFloat(8L)" should compile
        takesPosFloat(8L) shouldEqual 8.0F
        "takesPosFloat(8.0F)" should compile
        takesPosFloat(8.0F) shouldEqual 8.0F
      }

      it("should not compile when 0 is passed in") {
        "takesPosFloat(0)" shouldNot compile
        "takesPosFloat(0L)" shouldNot compile
        "takesPosFloat(0.0F)" shouldNot compile
      }

      it("should not compile when -8 is passed in") {
        "takesPosFloat(-8)" shouldNot compile
        "takesPosFloat(-8L)" shouldNot compile
        "takesPosFloat(-8.0F)" shouldNot compile
      }

      it("should not compile when x is passed in") {
        val x: Int = -8
        "takesPosFloat(x)" shouldNot compile
        val b: Long = -8L
        "takesPosFloat(b)" shouldNot compile
        val c: Float = -8.0F
        "takesPosFloat(c)" shouldNot compile
      }
    }

    it("should offer a unary + method that is consistent with Float") {
      forAll { (p: PosFloat) =>
        (+p).toFloat shouldEqual (+(p.toFloat))
      }
    }

    it("should offer a unary - method that returns NegFloat") {
      forAll { (p: PosFloat) =>
        (-p) shouldEqual (NegFloat.ensuringValid(-(p.toFloat)))
      }
    }

    it("should offer a 'plus' method that takes a PosZFloat and returns a PosFloat") {

      forAll { (posFloat: PosFloat, posZFloat: PosZFloat) =>
        (posFloat plus posZFloat) should === (PosFloat.ensuringValid(posFloat.value + posZFloat.value))
      }

      val examples =
        Table(
          (                "posFloat",                "posZFloat" ),
          (         PosFloat.MinValue,         PosZFloat.MinValue ),
          (         PosFloat.MinValue, PosZFloat.MinPositiveValue ),
          (         PosFloat.MinValue,         PosZFloat.MaxValue ),
          (         PosFloat.MinValue, PosZFloat.PositiveInfinity ),
          (         PosFloat.MaxValue,         PosZFloat.MinValue ),
          (         PosFloat.MaxValue, PosZFloat.MinPositiveValue ),
          (         PosFloat.MaxValue,         PosZFloat.MaxValue ),
          (         PosFloat.MaxValue, PosZFloat.PositiveInfinity ),
          ( PosFloat.PositiveInfinity,         PosZFloat.MinValue ),
          ( PosFloat.PositiveInfinity, PosZFloat.MinPositiveValue ),
          ( PosFloat.PositiveInfinity,         PosZFloat.MaxValue ),
          ( PosFloat.PositiveInfinity, PosZFloat.PositiveInfinity )
        )

      forAll (examples) { (a, b) =>
        (a plus b).value should be > 0.0f
      }

/*
error] /Users/bv/nobkp/delus/st-add-to-3.1.x/scalactic-test/src/test/scala/org/scalactic/anyvals/PosFloatSpec.scala:392: type mismatch;
[error]  found   : Double(3.0)
[error]  required: Float
[error]       (PosFloat(1.0) plus PosInt(2)) should === (PosFloat(3.0))
[error]                                                           ^

You know, I wonder if our macro could be friendlier and allow a Double literal for
specifying floats so long as it is in the valid range for floats.
*/
      // Sanity check that implicit widening conversions work too.
      (PosFloat(1.0f) plus PosInt(2)) should === (PosFloat(3.0f))
    }

    it("should offer overloaded 'sumOf' methods on the companion that take one PosFloat and one or more PosZFloats and returns a PosFloat") {

      forAll { (posFloat: PosFloat, posZFloat: PosZFloat) =>
        PosFloat.sumOf(posFloat, posZFloat) should === (PosFloat.ensuringValid(posFloat.value + posZFloat.value))
      }
      forAll { (posFloat: PosFloat, posZFloats: List[PosZFloat]) =>
        whenever(posZFloats.nonEmpty) {
          PosFloat.sumOf(posFloat, posZFloats.head, posZFloats.tail: _*) should === {
            PosFloat.ensuringValid(posFloat.value + posZFloats.head.value + posZFloats.tail.map(_.value).sum)
          }
        }
      }

      val posEdgeValues: List[PosFloat] = List(PosFloat.MinValue, PosFloat.MaxValue, PosFloat.PositiveInfinity)
      val posZEdgeValues = List(PosZFloat.MinValue, PosZFloat.MinPositiveValue, PosZFloat.MaxValue, PosZFloat.PositiveInfinity)
      // First put each PosFloat edge in front, then follow it with all permutations (orders) of all four PosZFloat edge values.
      Inspectors.forAll (posEdgeValues) { pos =>
        Inspectors.forAll (posZEdgeValues.permutations.toList) { case posZHead :: posZTail =>
          PosFloat.sumOf(pos, posZHead, posZTail: _*) should === {
            PosFloat.ensuringValid(pos.value + posZHead.value + posZTail.map(_.value).sum)
          }
        }
      }

      // Now do each PosFloat edge in front, then follow it with all combinations of 2 PosZEdgeFloats
      // I get all combos by doing combinations(2) ++ combinations(2).reverse. That seems to do the trick.
      val halfOfThePairs = posZEdgeValues.combinations(2).toList
      val posZPairCombos = halfOfThePairs ++ (halfOfThePairs.reverse)
      Inspectors.forAll (posEdgeValues) { pos =>
        Inspectors.forAll (posZPairCombos) { case posZHead :: posZTail  =>
          PosFloat.sumOf(pos, posZHead, posZTail: _*) should === {
            PosFloat.ensuringValid(pos.value + posZHead.value + posZTail.map(_.value).sum)
          }
        }
      }

      // Now do each PosFloat edge in front, then follow it with all combinations of 3 PosZEdgeFloats
      // I get all combos by doing combinations(3) ++ combinations(3).reverse. That seems to do the trick.
      val halfOfTheTriples = posZEdgeValues.combinations(3).toList
      val posZTripleCombos = halfOfTheTriples ++ (halfOfTheTriples.reverse)
      Inspectors.forAll (posEdgeValues) { pos =>
        Inspectors.forAll (posZTripleCombos) { case posZHead :: posZTail  =>
          PosFloat.sumOf(pos, posZHead, posZTail: _*) should === {
            PosFloat.ensuringValid(pos.value + posZHead.value + posZTail.map(_.value).sum)
          }
        }
      }
    }

    it("should offer 'min' and 'max' methods that are consistent with Float") {
      forAll { (pfloat1: PosFloat, pfloat2: PosFloat) =>
        pfloat1.max(pfloat2).toFloat shouldEqual pfloat1.toFloat.max(pfloat2.toFloat)
        pfloat1.min(pfloat2).toFloat shouldEqual pfloat1.toFloat.min(pfloat2.toFloat)
      }
    }

    it("should offer an 'isWhole' method that is consistent with Float") {
      forAll { (pfloat: PosFloat) =>
        pfloat.isWhole shouldEqual pfloat.toFloat.isWhole
      }
    }

    it("should offer 'round', 'ceil', and 'floor' methods that are consistent with Float") {
      forAll { (pfloat: PosFloat) =>
        // SKIP-SCALATESTJS,NATIVE-START
        pfloat.round.toFloat shouldEqual pfloat.toFloat.round
        // SKIP-SCALATESTJS,NATIVE-END
        pfloat.ceil.toFloat shouldEqual pfloat.toFloat.ceil
        pfloat.floor.toFloat shouldEqual pfloat.toFloat.floor
      }
    }

    it("should offer 'toRadians' and 'toDegrees' methods that are consistent with Float") {
      forAll { (pfloat: PosFloat) =>
        pfloat.toRadians shouldEqual pfloat.toFloat.toRadians
      }
    }

    // SKIP-SCALATESTJS,NATIVE-START
    it("should offer 'to' and 'until' method that is consistent with Float") {
      def rangeEqual(a: NumericRange[_], b: NumericRange[_]): Boolean =
        a.start == b.start && a.end == b.end && a.step == b.step

      forAll { (pfloat: PosFloat, end: Float, step: Float) =>
        rangeEqual(pfloat.until(end).by(1f), NumberCompatHelper.floatUntil(pfloat.toFloat, end).by(1f)) shouldBe true
        rangeEqual(pfloat.until(end, step), NumberCompatHelper.floatUntil(pfloat.toFloat, end, step)) shouldBe true
        rangeEqual(pfloat.to(end).by(1f), NumberCompatHelper.floatTo(pfloat.toFloat, end).by(1f)) shouldBe true
        rangeEqual(pfloat.to(end, step), NumberCompatHelper.floatTo(pfloat.toFloat, end, step)) shouldBe true
      }
    }
    // SKIP-SCALATESTJS,NATIVE-END

    it("should offer widening methods for basic types that are consistent with Float") {
      forAll { (pfloat: PosFloat) =>
        def widen(value: Float): Float = value
        widen(pfloat) shouldEqual widen(pfloat.toFloat)
      }
      forAll { (pfloat: PosFloat) =>
        def widen(value: Double): Double = value
        widen(pfloat) shouldEqual widen(pfloat.toFloat)
      }
      forAll { (pfloat: PosFloat) =>
        def widen(value: PosDouble): PosDouble = value
        widen(pfloat) shouldEqual widen(PosDouble.from(pfloat.toFloat).get)
      }
      forAll { (pfloat: PosFloat) =>
        def widen(value: PosZFloat): PosZFloat = value
        widen(pfloat) shouldEqual widen(PosZFloat.from(pfloat.toFloat).get)
      }
      forAll { (pfloat: PosFloat) =>
        def widen(value: PosZDouble): PosZDouble = value
        widen(pfloat) shouldEqual widen(PosZDouble.from(pfloat.toFloat).get)
      }
      forAll { (pfloat: PosFloat) =>
        def widen(value: NonZeroFloat): NonZeroFloat = value
        widen(pfloat) shouldEqual widen(NonZeroFloat.from(pfloat.toFloat).get)
      }
      forAll { (pfloat: PosFloat) =>
        def widen(value: NonZeroDouble): NonZeroDouble = value
        widen(pfloat) shouldEqual widen(NonZeroDouble.from(pfloat.toFloat).get)
      }
    }
  }
  it("should offer an ensuringValid method that takes a Float => Float, throwing AssertionError if the result is invalid") {
    PosFloat(33.0f).ensuringValid(_ + 1.0f) shouldEqual PosFloat(34.0f)
    PosFloat(33.0f).ensuringValid(_ => Float.PositiveInfinity) shouldEqual PosFloat.ensuringValid(Float.PositiveInfinity)
    an [AssertionError] should be thrownBy { PosFloat.MaxValue.ensuringValid(_ - PosFloat.MaxValue) }
    an [AssertionError] should be thrownBy { PosFloat.MaxValue.ensuringValid(_ => Float.NegativeInfinity) }
    an [AssertionError] should be thrownBy { PosFloat.MaxValue.ensuringValid(_ => Float.NaN) }
  }
}
  
