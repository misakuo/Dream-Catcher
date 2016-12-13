package me.moxun.dreamcatcher.connector.reporter;


import me.moxun.dreamcatcher.connector.inspector.protocol.module.Page;

public enum PrettyPrinterDisplayType {
  JSON(Page.ResourceType.XHR),
  HTML(Page.ResourceType.DOCUMENT),
  TEXT(Page.ResourceType.DOCUMENT), ;

  private final Page.ResourceType mResourceType;

  private PrettyPrinterDisplayType(Page.ResourceType resourceType) {
    mResourceType = resourceType;
  }

  /**
   * Converts PrettyPrinterDisplayType values to the appropriate
   *  {@link Page.ResourceType} values that DreamCatcher understands
   */
  public Page.ResourceType getResourceType() {
    return mResourceType;
  }
}
