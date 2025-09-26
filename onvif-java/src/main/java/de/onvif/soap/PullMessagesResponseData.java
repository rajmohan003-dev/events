package de.onvif.soap;


import java.util.Date;
import java.util.List;

public class PullMessagesResponseData {

    public final String topic;
    public final String operation;
    public final List<SimpleItem> Source;
    public final List<SimpleItem> Data;
    public final Date created;

    public PullMessagesResponseData(String topic,  List<SimpleItem> Source, List<SimpleItem> Data,String operation) {
        this.operation = operation;
        created = new Date();
        this.topic = topic;
        this.Source = Source;
        this.Data = Data;

    }
}
