package org.camunda.latera.bss.console

import org.camunda.latera.bss.logging.SimpleLogger
import org.codehaus.groovy.runtime.GStringImpl

class Console {
  SimpleLogger logger
  Boolean supressStdout
  Boolean supressStderr

  Console(LinkedHashMap params) {
    this.logger = new SimpleLogger(params.execution)
    params.remove('execution')

    this.supressStdout = false
    this.supressStderr = false

    if (params.supressStdout != null) {
      this.supressStdout = params.supressStdout
      params.remove('supressStdout')
    }
    if (params.supressStderr != null) {
      this.supressStderr = params.supressStderr
      params.remove('supressStderr')
    }
  }

  def runCommand(String command, List args = []) {
    def cmd = ([command] + args).join(' ')
    logger.info("Running command: ${cmd}")
    def stdout = new StringBuilder(), stderr = new StringBuilder()
    def proc = cmd.execute()
    proc.consumeProcessOutput(stdout, stderr)
    proc.waitFor()

    logger.info("Exit code: ${proc.exitValue()}")

    if (!supressStdout) {
      logger.info('Stdout:')
      logger.info(stdout)
    }

    if (!supressStderr) {
      if (proc.exitValue()) {
        logger.error('Sterr:')
        logger.error(stderr)
      } else {
        logger.info('Sterr:')
        logger.info(stderr)
      }
    }
    return [proc: proc, stdout: stdout, stderr: stderr]
  }

  def runCommand(GStringImpl command, List args = []) {
    return runCommand(command.toString(), args)
  }

  def runCommand(LinkedHashMap args, String command) {
    def arguments = []
    args.each { k, v ->
      if (k.length() == 1) {
        arguments += ["-${k} ${v}"]
      } else {
        arguments += ["--${k} ${v}"]
      }
    }
    return runCommand(command, arguments)
  }

  def runCommand(LinkedHashMap args, GStringImpl command) {
    return runCommand(args, command.toString())
  }
}