import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DebugXml {
    public static void main(String[] args) throws Exception {
        String xmlData = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" " +
            "xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
            "<ListRecords>" +
            "<record>" +
            "<header>" +
            "<identifier>oai:example.com:12345</identifier>" +
            "</header>" +
            "<metadata>" +
            "<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\">" +
            "<dc:title>Journal of Example Research</dc:title>" +
            "</oai_dc:dc>" +
            "</metadata>" +
            "</record>" +
            "</ListRecords>" +
            "</OAI-PMH>";
        
        System.out.println("XML Data:");
        System.out.println(xmlData);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlData.getBytes("UTF-8")));
        
        System.out.println("\nParsed document successfully");
        
        NodeList recordNodes = doc.getElementsByTagName("record");
        System.out.println("Found " + recordNodes.getLength() + " records");
        
        for (int i = 0; i < recordNodes.getLength(); i++) {
            Element recordElement = (Element) recordNodes.item(i);
            System.out.println("Processing record " + (i + 1));
            
            Element headerElement = (Element) recordElement.getElementsByTagName("header").item(0);
            if (headerElement != null) {
                NodeList identifierNodes = headerElement.getElementsByTagName("identifier");
                if (identifierNodes.getLength() > 0) {
                    System.out.println("Identifier: " + identifierNodes.item(0).getTextContent());
                }
            }
            
            Element metadataElement = (Element) recordElement.getElementsByTagName("metadata").item(0);
            if (metadataElement != null) {
                System.out.println("Found metadata element");
                Element dcElement = (Element) metadataElement.getElementsByTagName("dc").item(0);
                if (dcElement != null) {
                    System.out.println("Found dc element");
                    NodeList titleNodes = dcElement.getElementsByTagName("dc:title");
                    System.out.println("Found " + titleNodes.getLength() + " dc:title elements");
                    if (titleNodes.getLength() > 0) {
                        System.out.println("Title: " + titleNodes.item(0).getTextContent());
                    }
                } else {
                    System.out.println("No dc element found");
                }
            }
        }
    }
}
