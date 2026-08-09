package org.cscie88c.cli

import org.cscie88c.core.Utils

object Main extends App {
  if (args.nonEmpty) println(s" Message: ${Utils.greet(args(0))}")
  else println("Usage: run <name>")
}
