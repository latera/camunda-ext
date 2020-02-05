package org.camunda.bpm.pizzaorder;
import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.ServletProcessApplication;
import org.camunda.latera.bss.executionListeners.EventLogging;
import org.camunda.latera.bss.executionListeners.AutoSaveOrderData;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.delegate.DelegateExecution;
@ProcessApplication("pizzaOrderProcess")
public class PizzaOrderApplication extends ServletProcessApplication {
  public ExecutionListener getExecutionListener() {
    return new ExecutionListener() {
      public void notify(DelegateExecution execution) {
      new EventLogging().notify(execution);
        if (execution.getEventName().equals(ExecutionListener.EVENTNAME_END)) {
          new AutoSaveOrderData().notify(execution);
        }
    };
    };
  }
}
