package com.twitter.inject

import com.google.inject.{Guice, Module}
import com.twitter.app.{Flag, Flags}

trait CreateTwitterInjector {
  protected def modules: Seq[Module]

  protected def allowUndefinedFlags: Boolean = false

  protected def createInjector: Injector = createInjector(Array.empty[String])

  protected final def createInjector(args: String*): Injector = createInjector(args.toArray)

  protected final def createInjector(args: Array[String]): Injector = {
    val allModules = modules.flatMap(collectModules)
    val flags = new Flags("testing", includeGlobal = false)
    modules.flatMap(collectFlags).foreach(flags.add)
    flags.parseOrExit1(args, allowUndefinedFlags)
    Injector(Guice.createInjector(allModules: _*))
  }

  private def collectModules(module: Module): Seq[Module] = {
    module match {
      case m: TwitterBaseModule => module +: m.modules.flatMap(collectModules)
      case _ => Seq(module)
    }
  }

  private def collectFlags(module: Module): Seq[Flag[_]] = {
    module match {
      case m: TwitterBaseModule => m.flags ++ m.modules.flatMap(collectFlags)
      case _ => Seq.empty
    }
  }
}
