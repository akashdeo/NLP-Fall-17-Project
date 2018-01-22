import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Task2 {

	public static void createXML() throws IOException, ParserConfigurationException, TransformerException {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream("rural.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		// Splitting the corpus into articles
		StringBuilder sb = new StringBuilder();
		for (String line; (line = br.readLine()) != null;) {
			if (line.equals("")) {
				sb.append("-1");
			} else {
				sb.append(line.trim());
				if (!sb.toString().endsWith(".")) {
					sb.append('.');
				}
			}
		}

		String articles[] = sb.toString().split("-1");

		// creating the XML file here:
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("add");
		doc.appendChild(rootElement);

		for (int artNumber = 0; artNumber < articles.length; artNumber++) {
			String sentences[] = articles[artNumber].split("\\.|\\?");

			for (int sentenceNumber = 1; sentenceNumber < sentences.length; sentenceNumber++) {
				String[] words = sentences[sentenceNumber].split(" ");
				Vector<String> vector = new Vector<>();
				for (String w : words) {
					if (!w.equals("") && !w.equals("\"") && !w.equals("'") && !w.equals("'\"")) {
						vector.add(w.replace("\"", "").replace(";", "").replaceAll(":", "").replaceAll(",", "")
								.replaceAll("'", ""));
					}
				}

				if (!vector.isEmpty()) {
					// Sentence element
					Element sent = doc.createElement("doc");
					rootElement.appendChild(sent);

					// field1 elements
					Element field1 = doc.createElement("field");
					Attr attr1 = doc.createAttribute("name");
					attr1.setValue("ART_SENT");
					field1.setAttributeNode(attr1);

					field1.appendChild(doc.createTextNode((artNumber + 1) + "_" + (sentenceNumber)));
					sent.appendChild(field1);

					// field2 elements - all the words in the sentence
					for (String word : vector) {
						Element field2 = doc.createElement("field");
						Attr attr2 = doc.createAttribute("name");
						attr2.setValue("WORD");
						field2.setAttributeNode(attr2);
						field2.appendChild(doc.createTextNode(word));
						sent.appendChild(field2);
					}
				}
			}
		}

		// write the content into XML file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);

		// use your own file path to save the XML
		StreamResult result = new StreamResult(new File("file_rural.xml"));

		transformer.transform(source, result);

		System.out.println("File saved!");
	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerException {
		createXML();
	}
}
