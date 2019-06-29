package org.camunda.latera.bss.console

import org.camunda.latera.bss.logging.SimpleLogger

class Console {
  SimpleLogger logger
  Boolean supressStdout
  Boolean supressStderr

  Console(Map params) {
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

  Map runCommand(CharSequence command, List args = []) {
    List cmd = [command] + args
    logger.info("Running command: ${cmd.join(' ')}")
    StringBuilder stdout = new StringBuilder(), stderr = new StringBuilder()
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

  Map runCommand(Map args, CharSequence command) {
    List arguments = []
    args.each { k, v ->
      if (k.size() == 1) {
        arguments += ["-${k} ${v}"]
      } else {
        arguments += ["--${k} ${v}"]
      }
    }
    return runCommand(command, arguments)
  }
}