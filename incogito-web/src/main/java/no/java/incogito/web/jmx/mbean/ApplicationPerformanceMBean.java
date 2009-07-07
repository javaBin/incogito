package no.java.incogito.web.jmx.mbean;

import fj.P2;
import no.java.incogito.web.jmx.ApplicationPerformanceRecorder;
import no.java.incogito.web.jmx.ApplicationPerformanceRecorder.Reading;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@java.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ApplicationPerformanceMBean implements DynamicMBean {
    public ApplicationPerformanceRecorder recorder;

    private MBeanInfo beanInfo;

    public ApplicationPerformanceMBean(ApplicationPerformanceRecorder recorder) {
        this.recorder = recorder;

        List<MBeanAttributeInfo> attributeInfoList = new ArrayList<MBeanAttributeInfo>();
        for (P2<String, Reading> p2 : recorder.getReadings()) {
            attributeInfoList.add(new MBeanAttributeInfo(p2._1(), String.class.getName(), null, true, false, false));
        }

        System.out.println("attributeInfoList.size() = " + attributeInfoList.size());

        MBeanAttributeInfo[] attributes = attributeInfoList.toArray(new MBeanAttributeInfo[attributeInfoList.size()]);
        this.beanInfo = new MBeanInfo("java.lang.String", "Description, yo!", attributes,
                new MBeanConstructorInfo[0], new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
    }

    // -----------------------------------------------------------------------
    // DynamicMBean Implementation
    // -----------------------------------------------------------------------

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        Reading reading = recorder.getReading(attribute);

        if (reading == null) {
            throw new AttributeNotFoundException();
        }

        return toString(reading);
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new AttributeNotFoundException(attribute.getName());
    }

    public AttributeList getAttributes(String[] attributes) {
        P2<String, Reading>[] readings = recorder.getReadings();
        AttributeList list = new AttributeList(readings.length);
        for (P2<String, Reading> p2 : readings) {
            list.add(new Attribute(p2._1(), toString(p2._2())));
        }

        return list;
    }

    public AttributeList setAttributes(AttributeList attributes) {
        return new AttributeList();
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        throw new RuntimeException("Not implemented");
    }

    public MBeanInfo getMBeanInfo() {
        return beanInfo;
    }

    // -----------------------------------------------------------------------
    //
    // -----------------------------------------------------------------------

    private String toString(Reading reading) {
        return "lastExecutionTime=" + reading.lastElapsedTime + ":" +
                "invocations=" + reading.invocationCount + ":" +
                "exceptions=" + reading.exceptionCount;
    }
}
