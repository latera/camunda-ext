package org.camunda.latera.bss.helpers


import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.bpm.engine.variable.impl.VariableMapImpl

class Camunda {
  // TODO: Move to Task trait
  static def getTaskByName(DelegateExecution execution, String name) {
    def logger = new SimpleLogger(execution)
    logger.info("Trying to find task with name '${name}'")

    try {
      TaskService taskService = execution.getProcessEngineServices().getTaskService()
      Task task = taskService.createTaskQuery().processInstanceId(execution.getProcessInstanceId()).taskAssigned().taskName(name).singleResult()
      logger.info("Found task ${task.toString()}")

      return task
    } catch (Exception ex) {
      logger.info("Task not found!")
      return null
    }
  }

  static void updateTaskVariables(DelegateExecution execution, Task task, Map variables) {
    def logger = new SimpleLogger(execution)
    try {
      task.setVariables(variables)
      logger.info("Task variables successfully updated!")
    } catch (Exception ex) {
      logger.info("Error while updating task variables!")
    }
  }

  static void updateTaskVariable(DelegateExecution execution, Task task, String name, value) {
    def logger = new SimpleLogger(execution)
    try {
      task.setVariable(name, value)
      logger.info("Task variable ${name} successfully updated!")
    } catch (Exception ex) {
      logger.info("Error while updating task variable ${name}!")
    }
  }

  static void updateTaskVariablesLocal(DelegateExecution execution, Task task, Map variables) {
    def logger = new SimpleLogger(execution)
    try {
      task.setVariablesLocal(variables)
      logger.info("Task local variables successfully updated!")
    } catch (Exception ex) {
      logger.info("Error while updating task local variables!")
    }
  }

  static void updateTaskVariableLocal(DelegateExecution execution, Task task, String name, value) {
    def logger = new SimpleLogger(execution)
    try {
      task.setVariableLocal(name, value)
      logger.info("Task variable ${name} successfully updated!")
    } catch (Exception ex) {
      logger.info("Error while updating task variable ${name}!")
    }
  }

  static void submitTaskForm(DelegateExecution execution, Task task, Map variables = [:]) {
    def logger = new SimpleLogger(execution)
    try {
      FormService formService = execution.getProcessEngineServices().getFormService()
      formService.submitTaskForm(task.getId(), variables)
      logger.info("Task was successfully submitted!")
    } catch (Exception ex) {
      logger.info("Error while submitting task!")
    }
  }

  static void completeTask(DelegateExecution execution, Task task) {
    def logger = new SimpleLogger(execution)
    try {
      task.complete()
      logger.info("Task was successfully completed!")
    } catch (Exception ex) {
      logger.info("Error while compliting task!")
    }
  }

  static def getProgressTask(DelegateExecution execution) {
    def logger = new SimpleLogger(execution)
    def progressTaskName = execution.getVariable('progressTaskName')
    return getTaskByName(execution, progressTaskName)
  }

  static void refreshProgressTaskVariables(DelegateExecution execution) {
    def progressTask = getProgressTask(execution)
    updateTaskVariables(execution, progressTask, execution.getVariables())
  }

  static void closeProgressTask(DelegateExecution execution) {
    def progressTask = getProgressTask(execution)
    completeTask(execution, progressTask)
  }
}