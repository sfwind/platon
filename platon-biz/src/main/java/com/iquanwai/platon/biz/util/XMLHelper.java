package com.iquanwai.platon.biz.util;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Created by justin on 14-7-24.
 */
public class XMLHelper {
    public static <T> String createXML(T t){
        StringWriter sw = new StringWriter();
        try {
            JAXBContext jbc = JAXBContext.newInstance(t.getClass());   //传入要转换成xml的对象类型
            Marshaller mar = jbc.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);//是否格式化生成的xml串
            mar.setProperty(Marshaller.JAXB_FRAGMENT, true);
            mar.marshal(t, sw);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        String escaped_xml = sw.toString();

        return escaped_xml.replace("&lt;![CDATA", "<![CDATA").replace("]]&gt;", "]]>");
    }

    public static <T> T parseXml(Class<T> clazz, String xml) {
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Unmarshaller u = jc.createUnmarshaller();
            return (T) u.unmarshal(new StringReader(xml));
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String appendCDATA(String value){
        return "<![CDATA["+value+"]]>";
    }

}
