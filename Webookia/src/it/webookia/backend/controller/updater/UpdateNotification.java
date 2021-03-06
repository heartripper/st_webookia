package it.webookia.backend.controller.updater;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class manages the update notification giving them an object and
 * associating a list of changes.
 */
@XmlRootElement
public class UpdateNotification {
    private String object;
    private List<UpdateEntry> entry;

    /**
     * Class constructor
     */
    public UpdateNotification() {
        this.entry = new ArrayList<UpdateEntry>();
    }

    // Getter and setter

    @XmlElement(name = "object", required = true)
    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    @XmlElement(name = "entry", required = true)
    public List<UpdateEntry> getEntry() {
        return entry;
    }

    public void setEntry(List<UpdateEntry> entry) {
        this.entry = entry;
    }

    /**
     * This class manages directly the changed elements associating them to an
     * identifier and a date.
     * 
     */
    @XmlType
    public static class UpdateEntry {

        private String uid;
        private List<String> changed_fields;
        private Date time;

        /**
         * Class constructor
         */
        public UpdateEntry() {
            this.changed_fields = new ArrayList<String>();
        }

        // Getter and setter

        @XmlElement(name = "uid", required = true)
        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        @XmlElement(name = "changed_fields", required = true)
        public List<String> getChanged_fields() {
            return changed_fields;
        }

        public void setChanged_fields(List<String> changed_fields) {
            this.changed_fields = changed_fields;
        }

        @XmlElement(name = "time", required = true)
        public Date getTime() {
            return time;
        }

        public void setTime(Date date) {
            this.time = date;
        }
    }
}
