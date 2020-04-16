package org.camunda.latera.bss.testing

import org.camunda.bpm.engine.impl.pvm.runtime.ExecutionImpl
import org.camunda.bpm.engine.variable.impl.VariableMapImpl
import org.yaml.snakeyaml.Yaml

class TestExecution extends ExecutionImpl {
  public LinkedHashMap config
  public String testName

  TestExecution(String testName) {
    Yaml parser = new Yaml()
    this.config = parser.load(("/camunda/lib/execution.yml" as File).text)
    this.testName = testName
  }

  public Object getVariable(String variableName) {
    return this.config[this.testName][variableName]
  }

  public void setVariable(String variableName, Object value) {
    this.config[this.testName][variableName] = value
  }

  public VariableMapImpl getVariables() {
    return new VariableMapImpl(this.config[this.testName])
  }

  public String getProcessInstanceId() {
    return "Test instance"
  }
}
