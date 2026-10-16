package bayestensor
package driver

import utils._
import frontend._
import backend._
import transformation._
import ir._
import java.io._

object Driver {
  // var USE_SERVER = false
  var USE_SERVER = true
  val bns = Seq("insurance", "cancer", "hepar2", "alarm", "pigs", "survey", "water")
  val baselines = Seq("alarm", "evidence1", "evidence2", "grass", "noisyOr", 
                      "twoCoins", "murderMystery")
  def compile(filePath: String, linear: Boolean): String = {
    val exp = 
      SourceCode.fromFile(filePath).exp |>
      ObserveHoist |>
      DiceDesugar |>
      BayesTensorNormalizer |>
      new TensorLowering(linear)
    val npCode = 
      if(USE_SERVER) 
        NumpyCodeGenerator.codegen(exp)
      else 
        NumpyCodeGenerator(exp)
    npCode
  }
  def writeToFile(outputFile: String, npCode: String): Unit = {
    val pw = new PrintWriter(outputFile)
    pw.println(npCode)
    pw.close()
  }
  def getFileName(name: String, bn: Boolean): String = 
    if(bn)
      s"progs/dice/bns/$name.bif.dice"
    else
      s"progs/dice/$name.dice"
  def generate_baseline(outputFolder: String): Unit = {
    for(base <- baselines) {
      val filePath = getFileName(base, false)
      val npCode = compile(filePath, false)
      val outputFile = s"${outputFolder}/${base}.py"
      writeToFile(outputFile, npCode)
    }
  }
  def generate_bn(outputFolder: String): Unit = {
    for(bn <- bns) {
      val filePath = getFileName(bn, true)
      val npCode = compile(filePath, true)
      val outputFile = s"${outputFolder}/${bn}.py"
      writeToFile(outputFile, npCode)
    }
  }
  def runPython(npCode: String): String = {
    if(USE_SERVER)
      PythonProxy.runPythonCodeServer(npCode)
    else
      PythonProxy.runPythonCode(npCode)
  }
  def e2e(file: String, bn: Boolean): Unit = {
    val filePath = getFileName(file, bn) 
    val npCode = time(compile(filePath, bn), "compile")
    // println(npCode)
    // println(PythonProxy.runPythonCodeServer(npCode))
    val pyRes = time(runPython(npCode), "python exec")
    println(pyRes)
  }
  def main(args: Array[String]): Unit = {
    // PythonProxy.runPythonCodeServer("1 + 3")
    if(args.length == 0) {
      generate_bn("generated/bns")
      generate_baseline("generated/baselines")
    } else if (args.length == 2) {
      val file = args(0)
      val bn = args(1) match {
        case "bn" => true
        case "baseline" => false
        case _ => raise(s"Not acceptable second arg: ${args.mkString(",")}")
      }
      e2e(file, bn)
    } else {
      raise(s"Not acceptable args: ${args.mkString(",")}")
    }
  }
}