package org.camunda.latera.bss.taskListeners

import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.latera.bss.connectors.HOMS

class TaskNotifier implements TaskListener {
  void notify(DelegateTask task) {
    new HOMS(task.getExecution()).sendTaskEvent(task.getId(), task.getEventName())
  }
}
