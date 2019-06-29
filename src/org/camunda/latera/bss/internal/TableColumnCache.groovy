package org.camunda.latera.bss.internal

import java.util.concurrent.ConcurrentHashMap

class TableColumnCache {
  private Map<String, List> map;
  private static final TableColumnCache instance = new TableColumnCache();

  static TableColumnCache getInstance() {
    return instance;
  }

  private TableColumnCache() {
    map = new ConcurrentHashMap<String, List>();
  }

  void put(CharSequence key, List value) {
    if (key != null && value != null) {
      map[key.toString()] = value
    }
  }

  def get(CharSequence key) {
    return map[key.toString()]
  }

  def putAndGet(CharSequence key, def value) {
    put(key, value)
    return get(key)
  }
}