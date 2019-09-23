package org.camunda.latera.bss.internal

import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap
import static org.camunda.latera.bss.utils.Numeric.toIntSafe

class MessageCache {
  private Map<String, String> map;
  private static final MessageCache instance = new MessageCache();

  static MessageCache getInstance() {
    return instance;
  }

  private MessageCache() {
    map = new ConcurrentHashMap<String, String>();
  }

  void put(CharSequence key, def value) {
    if (key != null && value != null) {
      map[key.toString()] = value.toString()
    }
  }

  def get(CharSequence key) {
    return map[key.toString()]
  }

  def putAndGet(CharSequence key, def value) {
    put(key, value)
    return get(key)
  }

  def getKey(def value) {
    String val = value.toString()
    return map.find{it.value == val}?.key
  }

  String putAndGetKey(CharSequence key, def value) {
    put(key, value)
    return key.toString()
  }
}