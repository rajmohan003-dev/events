package org.onvif.client;

import de.onvif.soap.OnvifDevice;
import java.io.File;
import java.io.FileInputStream;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import de.onvif.soap.OnvifServiceFactory;
import jakarta.xml.ws.BindingProvider;
import org.apache.commons.io.FileUtils;
import org.onvif.ver10.events.wsdl.*;
import org.onvif.ver10.schema.CapabilityCategory;

import javax.xml.datatype.DatatypeFactory;

public class SimpleTest {

  // This test reads connection params from a properties file and take a
  // screenshot
  public static void main(String[] args) throws Exception {

    final Map<String, OnvifDevice> onvifCameras = new HashMap<>();
    final Map<String, OnvifCredentials> credentialsMap = new HashMap<>();
    final String propFileRelativePath = "onvif-java/src/test/resources/onvif.properties";
    final Properties config = new Properties();
    final File f = new File(propFileRelativePath);
    if (!f.exists()) throw new Exception("fnf: " + f.getAbsolutePath());
    config.load(new FileInputStream(f));

    for (Object k : config.keySet()) {
      String line = config.get(k.toString()).toString();
      OnvifCredentials credentials = GetTestDevice.parse(line);
      if (credentials != null) {
        try {
          System.out.println("Connect to camera, please wait ...");
          OnvifDevice cam =
              new OnvifDevice(
                  credentials.getHost(), credentials.getUser(), credentials.getPassword());
          System.out.printf("Connected to device %s (%s)%n", cam.getDeviceInfo(), k);
//          PullPointSubscription pullPointSubscription ;
//
//          org.onvif.ver10.schema.Capabilities capabilities = cam.getCapabilities();
//          if (capabilities == null) {
//            throw new ConnectException("Capabilities not reachable.");
//          }
//System.out.println(cam.getEvents().getServiceCapabilities());
//
//          BindingProvider eventsServicePort = (BindingProvider) cam.eventService.getPullPointSubscription();
//          pullPointSubscription = OnvifServiceFactory.createServiceProxy(
//                  eventsServicePort,
//                  capabilities.getEvents().getXAddr(),
//                  PullPointSubscription.class,
//                   cam.securityHandler,
//                  OnvifDevice.isVerbose()
//          );
//
//          PullMessages pullMessages = new PullMessages();
//
//          pullMessages.setTimeout(DatatypeFactory.newInstance().newDuration("PT10S"));
//          pullMessages.setMessageLimit(10);

//              System.out.println(pullPointSubscription.pullMessages(pullMessages));
            EventPortType eventPort = cam.getEvents();
            GetEventPropertiesResponse props = eventPort.getEventProperties(new GetEventProperties());

            System.out.println("Supported Topics:");

            printTopics(props.getTopicSet().getAny(),"");
        } catch (Throwable th) {System.err.println("Error on device: " + k);
          th.printStackTrace();
        }
      }
    }
  }
    private static void printTopics(List<Object> nodes, String path) {
        for (Object node : nodes) {
            if (node instanceof org.w3c.dom.Element) {
                org.w3c.dom.Element el = (org.w3c.dom.Element) node;
                String name = el.getLocalName();
                String newPath = path.isEmpty() ? name : path + "/" + name;
                System.out.println(newPath);

                // go deeper
                org.w3c.dom.NodeList children = el.getChildNodes();
                List<Object> childList = new java.util.ArrayList<>();
                for (int i = 0; i < children.getLength(); i++) {
                    if (children.item(i) instanceof org.w3c.dom.Element) {
                        childList.add(children.item(i));
                    }
                }
                printTopics(childList, newPath);
            }
        }
    }

}
