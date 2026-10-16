package bayestensor
package utils

import scala.sys.process._
import java.net._
import java.io._
import scala.io._

object PythonProxy {
  def runPythonCode(pythonCode: String): String = {
    val pr = Process(Seq("python", "-c", pythonCode))
    // val tmpFile = "tmp_python_output.txt"
    // (pr #> new java.io.File(tmpFile)).run()
    // Thread.sleep(1000)
    // scala.io.Source.fromFile(tmpFile).mkString
    pr.!!
  }

  def runPythonCodeServer(pythonCode: String): String = {
    val sock = new Socket(InetAddress.getByName("localhost"), 10042)
    val in = new BufferedSource(sock.getInputStream()).getLines()
    val out = new PrintStream(sock.getOutputStream())
    val msg = pythonCode + "    <END>"
    out.print(msg)
    // out.flush()
    // out.println()
    out.flush()
    // out.close()
    val result = in.next()
    println("Received: " + result)
    sock.close()
    result
  }

}