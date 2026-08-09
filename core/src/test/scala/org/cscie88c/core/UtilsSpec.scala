package org.cscie88c.core

import org.scalatest.funsuite.AnyFunSuite

class UtilsSpec extends AnyFunSuite {
  test("greet should return correct greeting") {
    assert(Utils.greet("Scala") == "Hello, Scala!")
  }
}
