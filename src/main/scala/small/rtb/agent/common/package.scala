package small.rtb.agent
import java.io.File

package object common {
  /** An alias for the `Any` type.
   * Denotes that the type should be filled in.
   */
  type ??? = Any

  /** An alias for the `Nothing` type.
   * Denotes that the type should be filled in.
   */
  type *** = Nothing

  /** An  union type function example with String and Int */
  def unionStringAndInt[A](a: A)(implicit ev: (Int with String) <:< A) = a match {
    case i: Int => i
    case s: String => s
  }

  /**
   * Get a resource from the `target` directory. Eclipse does not copy
   * resources to the output directory, then the class loader cannot find them.
   */
  def resourceAsStreamFromSrc(resourcePath: List[String]): Option[java.io.InputStream] = {
    val classesDir = new File(getClass.getResource(".").toURI)
    val projectDir = classesDir.getParentFile.getParentFile.getParentFile.getParentFile
    val resourceFile = subFile(projectDir, resourcePath: _*)
    if (resourceFile.exists)
      Some(new java.io.FileInputStream(resourceFile))
    else
      None
  }

  /**
   * Get a child of a file. For example,
   *
   * subFile(homeDir, "b", "c")
   *
   * corresponds to ~/b/c
   */
  def subFile(file: File, children: String*) = {
    children.foldLeft(file)((file, child) => new File(file, child))
  }
}
