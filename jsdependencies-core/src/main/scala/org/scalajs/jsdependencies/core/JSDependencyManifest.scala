package org.scalajs.jsdependencies.core

import org.scalajs.core.tools.json._
import org.scalajs.core.tools.io._

import scala.collection.immutable.{Seq, Traversable}

import java.io.{Reader, Writer}

/** The information written to a "JS_DEPENDENCIES" manifest file. */
final class JSDependencyManifest(
    val origin: Origin,
    val libDeps: List[JSDependency],
    val requiresDOM: Boolean) {

  import JSDependencyManifest._

  override def equals(that: Any): Boolean = that match {
    case that: JSDependencyManifest =>
      this.origin == that.origin &&
      this.libDeps == that.libDeps &&
      this.requiresDOM == that.requiresDOM
    case _ =>
      false
  }

  override def hashCode(): Int = {
    import scala.util.hashing.MurmurHash3._
    var acc = HashSeed
    acc = mix(acc, origin.##)
    acc = mix(acc, libDeps.##)
    acc = mixLast(acc, requiresDOM.##)
    finalizeHash(acc, 3)
  }

  override def toString(): String = {
    val b = new StringBuilder
    b ++= s"JSDependencyManifest(origin=$origin"
    if (libDeps.nonEmpty)
      b ++= s", libDeps=$libDeps"
    if (requiresDOM)
      b ++= s", requiresDOM=$requiresDOM"
    b ++= ")"
    b.result()
  }
}

object JSDependencyManifest {

  // "org.scalajs.jsdependencies.core.JSDependencyManifest".##
  private final val HashSeed = -902988673

  final val ManifestFileName = "JS_DEPENDENCIES"

  implicit object JSDepManJSONSerializer extends JSONSerializer[JSDependencyManifest] {
    @inline def optList[T](x: List[T]): Option[List[T]] =
      if (x.nonEmpty) Some(x) else None

    def serialize(x: JSDependencyManifest): JSON = {
      new JSONObjBuilder()
        .fld("origin", x.origin)
        .opt("libDeps", optList(x.libDeps))
        .opt("requiresDOM", if (x.requiresDOM) Some(true) else None)
        .toJSON
    }
  }

  implicit object JSDepManJSONDeserializer extends JSONDeserializer[JSDependencyManifest] {
    def deserialize(x: JSON): JSDependencyManifest = {
      val obj = new JSONObjExtractor(x)
      new JSDependencyManifest(
          obj.fld[Origin]("origin"),
          obj.opt[List[JSDependency]]("libDeps").getOrElse(Nil),
          obj.opt[Boolean]("requiresDOM").getOrElse(false))
    }
  }

  def write(dep: JSDependencyManifest, output: WritableVirtualTextFile): Unit = {
    val writer = output.contentWriter
    try write(dep, writer)
    finally writer.close()
  }

  def write(dep: JSDependencyManifest, writer: Writer): Unit =
    writeJSON(dep.toJSON, writer)

  def read(file: VirtualTextFile): JSDependencyManifest = {
    val reader = file.reader
    try read(reader)
    finally reader.close()
  }

  def read(reader: Reader): JSDependencyManifest =
    fromJSON[JSDependencyManifest](readJSON(reader))

}
