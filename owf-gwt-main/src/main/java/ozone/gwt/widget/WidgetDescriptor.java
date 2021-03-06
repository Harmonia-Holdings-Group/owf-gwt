package ozone.gwt.widget;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.Widget;

public final class WidgetDescriptor extends JavaScriptObject {

  protected WidgetDescriptor() {}

  public static WidgetDescriptor create(WidgetHandle widgetHandle) {
    return create(widgetHandle, null);
  }
  
  public static WidgetDescriptor create(WidgetHandle widgetHandle, String displayGroupName) {
    return create(widgetHandle, displayGroupName, null);
  }
  
  public static WidgetDescriptor create(WidgetHandle widgetHandle, String displayGroupName, String prefix) {
    
    String widgetName = widgetHandle.getGWTIsWidget().getClass().getSimpleName();
    
    String universalName = makeUniversalName(widgetHandle);
    if (prefix != null) {
      universalName = prefix+"."+universalName;
    }
    
    StringBuffer  displayName = new StringBuffer();
    if (prefix != null) {
      displayName.append(prefix);
      displayName.append(' ');
    }
    
    int len = widgetName.length();
    for (int i = 1; i < len; i++) {
      char prev = widgetName.charAt(i-1);
      char ch = widgetName.charAt(i);
      displayName.append(prev);
      if (i < len) {
        char next = widgetName.charAt(i+1);
        if (   ( Character.isUpperCase(ch) && !Character.isUpperCase(next))
            || (!Character.isLetter(ch)   &&   Character.isLetter(next)) ) {
          displayName.append(' ');
        }
      }
    }
    displayName.append(widgetName.charAt(widgetName.length()-1));
    
    if (displayGroupName != null && displayGroupName.trim().length() > 0) {
      displayName.append(" - ");
      displayName.append(displayGroupName);
    }
    
    Widget gwtWidget = widgetHandle.getGWTIsWidget().asWidget();
    
    int height = 0;
    int width = 0;
    
    while ((height == 0 || width == 0) && gwtWidget != null) {
      height = gwtWidget.getOffsetHeight();
      width = gwtWidget.getOffsetWidth();
      gwtWidget = gwtWidget.getParent();
    }
    
    // OWF typically adds some chrome and title/security bars, so we reduce the size a little to accommodate.
    // Also, OWF's Widget Editor requires widget height/width to be at least 200 pixels.
    height = (int) Math.max(200, (height*0.90));
    width = (int) Math.max(200, (width*0.90));
    
    JsArray<IntentDescriptor> intentDescriptors = JsArray.createArray().cast();
    for (Intent<?> intent : widgetHandle.getIntentsReceived()) {
      intentDescriptors.push(IntentDescriptor.create(intent.getAction(), intent.getDataType()));
    }
    
    return nativeCreate(
      widgetName,
      universalName.toString(),
      displayName.toString(),
      height,
      width,
      intentDescriptors
    );
  }
  
  /**
   * The template values like %appUrlPrefix% are used by the mkdesc.sh script, 
   * which is generated by the global function __OwfGwtSaveDescriptors(), which
   * can be executed while running in DirectWidgetFramework mode.
   * @param widgetName
   * @param universalName
   * @param displayName
   * @param height
   * @param width
   * @param intentDescriptors
   * @return
   */
  private static native WidgetDescriptor nativeCreate(
        String widgetName,
        String universalName,
        String displayName,
        int height,
        int width,
        JsArray<IntentDescriptor> intentDescriptors
      ) /*-{
    return {
      _widgetName: widgetName, // not part of standard OWF widget descriptor
      widgetUrl: "%appUrlPrefix%owfGwtWidget.html#"+widgetName+"%appParams%",
      imageUrlLarge: "%appUrlPrefix%owfWidgets/icons/"+widgetName+"128.png",
      imageUrlSmall: "%appUrlPrefix%owfWidgets/icons/"+widgetName+"128.png",
      universalName: universalName,
      displayName: displayName,
      height: height,
      width: width,
      visible: true,
      singleton: false,
      background: false,
      widgetTypes: ["standard"],
      intents: {
        receive: intentDescriptors
      }
    };
  }-*/;
  
  public native String getWidgetName() /*-{
    return this._widgetName;
  }-*/;
  
  public native String getUniversalName() /*-{
    return this.universalName;
  }-*/;
  
  public native JsArray<IntentDescriptor> getReceivedIntents() /*-{
    return this.intents.receive;
  }-*/;

  public static String makeUniversalName(WidgetHandle widgetHandle) {
    StringBuffer universalName = new StringBuffer();
    String[] components = widgetHandle.getGWTIsWidget().getClass().getName().split("\\.");
    for (int i = components.length-1; i >= 0; i--) {
      universalName.append(components[i]);
      if (i > 0) {
        universalName.append('.');
      }
    }
    return universalName.toString();
  }
}
