package org.onvif.client;

import de.onvif.soap.OnvifDevice;
import de.onvif.soap.ProcessedPullMessagesResponse;
import de.onvif.soap.PullMessagesCallbacks;
import de.onvif.soap.PullPointSubscriptionHandler;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.soap.SOAPException;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.onvif.ver10.events.wsdl.*;
import org.onvif.ver10.events.wsdl.CreatePullPointSubscription.SubscriptionPolicy;
import org.onvif.ver10.schema.Capabilities;
import org.onvif.ver10.schema.CapabilityCategory;
import org.onvif.ver10.schema.Profile;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PullPointTest implements PullMessagesCallbacks {
    private PullPointTest() {

    }
    static private PullPointTest theInstance = new PullPointTest();

    public static void main(String[] args) throws IOException {
        theInstance = new PullPointTest();
        theInstance.run(args);
    }

    public void run(String[] args) throws IOException {
        OnvifCredentials creds = GetTestDevice.getOnvifCredentials(args);
        System.out.println("Connect to camera, please wait ...");

        OnvifDevice cam = null;
        try {
            cam = new OnvifDevice(creds.getHost(), creds.getUser(), creds.getPassword());
        } catch (ConnectException | SOAPException e1) {
            System.err.println("No connection to device with ip " + creds + ", please try again.");
            System.exit(0);
        } catch (URISyntaxException e) {
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }

        org.oasis_open.docs.wsn.b_2.ObjectFactory objectFactory =
                new org.oasis_open.docs.wsn.b_2.ObjectFactory();
        CreatePullPointSubscription pullPointSubscription = new CreatePullPointSubscription();
        FilterType filter = new FilterType();
        TopicExpressionType topicExp = new TopicExpressionType();
        topicExp.getContent().add("tns1:Device/Trigger/DigitalInput//."); // every event in that
        // topic
        topicExp.setDialect("http://www.onvif.org/ver10/tev/topicExpression/ConcreteSet");
        JAXBElement<?> topicExpElem = objectFactory.createTopicExpression(topicExp);
        filter.getAny().add(topicExpElem);
        pullPointSubscription.setFilter(filter);
        ObjectFactory eventObjFactory =
                new ObjectFactory();
        SubscriptionPolicy subcriptionPolicy =
                eventObjFactory.createCreatePullPointSubscriptionSubscriptionPolicy();
        pullPointSubscription.setSubscriptionPolicy(subcriptionPolicy);
        String timespan = "PT1M"; // every 1 minute
        pullPointSubscription.setInitialTerminationTime(
                objectFactory.createSubscribeInitialTerminationTime(timespan));

        try {
            PullPointSubscriptionHandler ppsh = new PullPointSubscriptionHandler(cam, pullPointSubscription, this);
            Thread.currentThread().join();
            ppsh.setTerminate();


         } catch (Exception e) {
            System.out.println(e.getClass().getName() + " " + e.getMessage());
        }
    }



    Map<Date, ProcessedPullMessagesResponse> responses = new HashMap<>();

    @Override
    public void onPullMessagesReceived(PullMessagesResponse pullMessages) {
        ProcessedPullMessagesResponse ppmr = new ProcessedPullMessagesResponse(pullMessages);

        ppmr.responseData.forEach(x -> {
            x.Data.forEach(data -> {
                String topic = x.topic;
                String name = data.Name;
                String value = data.Value;
                String operation = x.operation; // original PropertyOperation

                System.out.println(x.created + " " + topic + " " + name + " " + value + " Operation=" + operation);

                // Detect panic button press only if operation is "Changed"
                if (topic.contains("Device/Trigger/DigitalInput") &&
                        "LogicalState".equalsIgnoreCase(name) &&
                        "true".equalsIgnoreCase(value) &&
                        "Changed".equalsIgnoreCase(operation)) {

                    System.out.println("🚨 Panic button PRESSED! Operation=" + operation);
                }


            });
        });
    }
}
