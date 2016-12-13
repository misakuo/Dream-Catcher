package me.moxun.dreamcatcher.connector.reporter;

/**
 * Interface that is called if AsyncPrettyPrinterRegistry is unpopulated when
 * the first peer connects to DreamCatcher. It is responsible for registering header
 * names and their corresponding pretty printers
 */
public interface AsyncPrettyPrinterInitializer {

  /**
   * Populates AsyncPrettyPrinterRegistry with header names and their corresponding pretty
   * printers. This is responsible for registering all {@link AsyncPrettyPrinter} to the
   * provided registry.
   * @param registry
   */
  void populatePrettyPrinters(AsyncPrettyPrinterRegistry registry);

}
