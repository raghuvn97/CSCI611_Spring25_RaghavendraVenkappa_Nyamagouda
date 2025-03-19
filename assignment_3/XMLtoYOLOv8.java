import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;

public class XMLtoYOLOv8 {

    public static void main(String[] args) {
        // Directory containing the XML files
        File xmlDirectory = new File("/annotations");

        // Output directory for YOLOv8 annotation files
        File outputDirectory = new File("/annotation_txt");
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs(); // Create the output directory if it doesn't exist
        }

        // Process all XML files in the directory
        File[] xmlFiles = xmlDirectory.listFiles((dir, name) -> name.endsWith(".xml"));
        if (xmlFiles != null) {
            for (File xmlFile : xmlFiles) {
                try {
                    // Parse the XML file
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.parse(xmlFile);
                    doc.getDocumentElement().normalize();

                    // Get the image dimensions
                    Element sizeElement = (Element) doc.getElementsByTagName("size").item(0);
                    int width = Integer.parseInt(sizeElement.getElementsByTagName("width").item(0).getTextContent());
                    int height = Integer.parseInt(sizeElement.getElementsByTagName("height").item(0).getTextContent());

                    // Get the list of objects
                    NodeList objectList = doc.getElementsByTagName("object");

                    // Create a corresponding output .txt file
                    String outputFileName = xmlFile.getName().replace(".xml", ".txt");
                    File outputFile = new File(outputDirectory, outputFileName);
                    FileWriter writer = new FileWriter(outputFile);

                    // Iterate through each object
                    for (int i = 0; i < objectList.getLength(); i++) {
                        Node objectNode = objectList.item(i);
                        if (objectNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element objectElement = (Element) objectNode;

                            // Get the class name and map it to a class ID
                            String className = objectElement.getElementsByTagName("name").item(0).getTextContent();
                            int classId = getClassId(className); // Implement this method to map class names to IDs

                            // Get the bounding box coordinates
                            Element bndboxElement = (Element) objectElement.getElementsByTagName("bndbox").item(0);
                            int xmin = Integer.parseInt(bndboxElement.getElementsByTagName("xmin").item(0).getTextContent());
                            int ymin = Integer.parseInt(bndboxElement.getElementsByTagName("ymin").item(0).getTextContent());
                            int xmax = Integer.parseInt(bndboxElement.getElementsByTagName("xmax").item(0).getTextContent());
                            int ymax = Integer.parseInt(bndboxElement.getElementsByTagName("ymax").item(0).getTextContent());

                            // Convert to YOLOv8 format
                            double xCenter = (xmin + xmax) / 2.0 / width;
                            double yCenter = (ymin + ymax) / 2.0 / height;
                            double boxWidth = (xmax - xmin) / (double) width;
                            double boxHeight = (ymax - ymin) / (double) height;

                            // Write the YOLOv8 formatted annotation to the output file
                            writer.write(classId + " " + xCenter + " " + yCenter + " " + boxWidth + " " + boxHeight + "\n");
                        }
                    }

                    // Close the writer
                    writer.close();
                    System.out.println("Processed: " + xmlFile.getName());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("No XML files found in the directory.");
        }
    }

    // Method to map class names to class IDs
    private static int getClassId(String className) {
        // Example mapping (you can customize this based on your dataset)
        switch (className) {
            case "trafficlight":
                return 0;
            // Add more cases as needed
            default:
                return -1; // Unknown class
        }
    }
}
