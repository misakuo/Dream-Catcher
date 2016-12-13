package me.moxun.dreamcatcher.connector;


import me.moxun.dreamcatcher.connector.inspector.protocol.ChromeDevtoolsDomain;

public interface InspectorModulesProvider {
  Iterable<ChromeDevtoolsDomain> get();
}
