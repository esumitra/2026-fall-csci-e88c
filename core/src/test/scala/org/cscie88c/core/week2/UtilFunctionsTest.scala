package org.cscie88c.core.week2

import org.cscie88c.core.testutils.{StandardTest}

class UtilFunctionsTest extends StandardTest {
  
  "UtilFunctions" when {
    "isPalindrome" should {
      "return true for a palindrome word" in {
        UtilFunctions.isPalindrome("tenet") should be (true)
      }
    }

    // add unit tests for median below
    
  }
}
