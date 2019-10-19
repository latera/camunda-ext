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
    String progressTaskName = execution.getVariable('progressTaskName') ?: 'Progress'
    return getTaskByName(progressTaskName)
  }

  CamundaTask getWaitTask() {
    String progressTaskName = execution.getVariable('waitTaskName') ?: 'Wait'
    return getTaskByName(progressTaskName)
  }

  CamundaTask getCheckTask() {
    String progressTaskName = execution.getVariable('checkTaskName') ?: 'Check'
    return getTaskByName(progressTaskName)
  }

  Boolean refreshProgressTaskVariables() {
    CamundaTask progressTask = getProgressTask()
    return updateTaskVariables(progressTask, execution.getVariables())
  }

  Boolean closeProgressTask() {
    CamundaTask task = getProgressTask()
    return completeTask(task)
  }

  Boolean closeWaitTask() {
    CamundaTask task = getWaitTask()
    return completeTask(task)
  }

  Boolean closeCheckTask() {
    CamundaTask task = getCheckTask()
    return completeTask(task)
  }

  Boolean closeTask(String name) {
    CamundaTask task = getTaskByName(name)
    return completeTask(task)
  }
}
