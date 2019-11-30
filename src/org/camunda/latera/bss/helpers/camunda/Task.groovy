package org.camunda.latera.bss.helpers.camunda

import org.camunda.bpm.engine.FormService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.task.Task as CamundaTask

trait Task {
  CamundaTask getTaskByName(String name) {
    logger.info("Trying to find task with name '${name}'")

    try {
      TaskService taskService = execution.getProcessEngineServices().getTaskService()
      CamundaTask task = taskService.createTaskQuery().processInstanceId(execution.getProcessInstanceId()).taskAssigned().taskName(name).singleResult()
      logger.info("Found task ${task.toString()}")

      return task
    } catch (Exception ex) {
      logger.info("Task not found!")
      return null
    }
  }

  Boolean updateTaskVariables(CamundaTask task, Map variables) {
    try {
      task.setVariables(variables)
      logger.info("Task variables successfully updated!")
      return true
    } catch (Exception ex) {
      logger.info("Error while updating task variables!")
      return false
    }
  }

  Boolean updateTaskVariable(CamundaTask task, String name, def value) {
    try {
      task.setVariable(name, value)
      logger.info("Task variable ${name} successfully updated!")
      return true
    } catch (Exception ex) {
      logger.info("Error while updating task variable ${name}!")
      return false
    }
  }

  Boolean submitTaskForm(CamundaTask task, Map variables = [:]) {
    try {
      FormService formService = execution.getProcessEngineServices().getFormService()
      formService.submitTaskForm(task.getId(), variables)
      logger.info("Task was successfully submitted!")
      return true
    } catch (Exception ex) {
      logger.info("Error while submitting task!")
      return false
    }
  }

  Boolean completeTask(CamundaTask task) {
    try {
      task.complete()
      logger.info("Task was successfully completed!")
      return true
    } catch (Exception ex) {
      logger.info("Error while compliting task!")
      return false
    }
  }

  CamundaTask getProgressTask() {
    return getTaskByName(execution.getVariable('progressTaskName') ?: 'Progress')
  }

  CamundaTask getWaitTask() {
    return getTaskByName(execution.getVariable('waitTaskName') ?: 'Wait')
  }

  CamundaTask getCheckTask() {
    return getTaskByName(execution.getVariable('checkTaskName') ?: 'Check')
  }

  CamundaTask getTimerTask() {
    return getTaskByName(execution.getVariable('timerTaskName') ?: 'Timer')
  }

  Boolean refreshProgressTaskVariables() {
    return updateTaskVariables(getProgressTask(), execution.getVariables())
  }

  Boolean closeProgressTask() {
    return completeTask(getProgressTask())
  }

  Boolean closeWaitTask() {
    return completeTask(getWaitTask())
  }

  Boolean closeCheckTask() {
    return completeTask(getCheckTask())
  }

  Boolean closeTimerTask() {
    return completeTask(getTimerTask())
  }

  Boolean closeTask(String name) {
    return completeTask(getTaskByName(name))
  }
}
