package igx.server;

import java.net.*;

/**
 * This type was created in VisualAge.
 */
public class CustomRobot {
  public static Class getRobot(URL url, String file, int skillLevel) throws Exception {
	URL[] urls = {url};
	Class botClass;
	URLClassLoader ucl = new URLClassLoader(urls);
	try {
	  botClass = Class.forName(file, true, ucl);
	} catch (ClassNotFoundException e) {
	  return null;
	} catch (NoClassDefFoundError f) {
	  System.out.println(f.toString());
	  return null;
	}
	return botClass;
  }
}