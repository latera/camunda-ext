package org.camunda.latera.bss.console

import org.camunda.latera.bss.logging.SimpleLogger
import static org.camunda.latera.bss.utils.ListUtil.firstNotNull

class Console {
  SimpleLogger logger
  Boolean supressStdout
  Boolean supressStderr

  Console(Map params) {
    this.logger = new SimpleLogger(params.execution)
    def ENV     = System.getenv()

    this.supressStdout = Boolean.valueOf(firstNotNull([
      params.supressStdout,
      params.execution.getVariable('consoleSupressStdout'),
      params.execution.getVariable('consoleSupress'),
      ENV['CONSOLE_SUPRESS_STDOUT'],
      ENV['CONSOLE_SUPRESS']
    ], false))

    this.supressStderr = Boolean.valueOf(firstNotNull([
      params.supressStderr,
      params.execution.getVariable('consoleSupressStderr'),
      params.execution.getVariable('consoleSupress'),
      ENV['CONSOLE_SUPRESS_STDERR'],
      ENV['CONSOLE_SUPRESS']
    ], false))
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
      logger.debug('Stdout:')
      logger.debug(stdout)
    }

    if (!supressStderr) {
      if (proc.exitValue()) {
        logger.debug('Sterr:')
        logger.debug(stderr)
      } else {
        logger.debug('Sterr:')
        logger.debug(stderr)
      }
    }
    return [proc: proc, stdout: stdout, stderr: stderr]
  }

  private Boolean isShortCommand(CharSequence key) {
    return key.size() == 1
  }

  Map runCommand(Map args, CharSequence command) {
    List arguments = []
    args.each { key, value ->
      if (isShortCommand(key)) {
        arguments += ["-${key} ${value}"]
      } else {
        arguments += ["--${key} ${value}"]
      }
    }
    return runCommand(command, arguments)
  }
}
