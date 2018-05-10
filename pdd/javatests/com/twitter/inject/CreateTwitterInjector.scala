/*
 * PDD is a platform for privacy-preserving Web searches collection.
 * Copyright (C) 2016-2018 Vincent Primault <v.primault@ucl.ac.uk>
 *
 * PDD is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PDD is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PDD.  If not, see <http://www.gnu.org/licenses/>.
 */

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
