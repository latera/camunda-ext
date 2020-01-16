package org.camunda.latera.bss.taskListeners

import org.camunda.bpm.engine.identity.UserQuery
import org.camunda.bpm.engine.delegate.TaskListener
import org.camunda.bpm.engine.delegate.DelegateTask
import org.camunda.latera.bss.connectors.HOMS

class TaskNotifier implements TaskListener {
  void notify(DelegateTask task) {
    String assignee         = getAssignee(task)
    List<String> candidates = getCandidates(task)

    new HOMS(task.getExecution()).sendTaskEvent(
      task.getId(),
      task.getEventName(),
      [*candidates, assignee].unique(false) - null
    )
  }

  private static String getAssignee(DelegateTask task) {
    String assigneeId = task.getAssignee()

    if (assigneeId) {
      return task.getExecution()
                 .getProcessEngineServices()
                 .getIdentityService()
                 .createUserQuery()
                 .userId(assigneeId)
                 .singleResult()
                 .getEmail()
    } else {
      return null
    }
  }

  private static List<String> getCandidates(DelegateTask task) {
    List<String> candidateGroups = task.getCandidates().collect { it.getGroupId() }
    UserQuery query              = task.getExecution()
                                       .getProcessEngineServices()
                                       .getIdentityService()
                                       .createUserQuery()

    candidateGroups.inject([]) { list, group ->
      if (group) {
        [*list, *query.memberOfGroup(group).list().collect { it.getEmail() }]
      }
    }
  }
}
