/*
 * File: XmlReader.java
 * Creation: 2011_12_14
 * Author: Hj. Malthaner <h_malthaner@users.sourceforge.net>
 * License: See license.txt
 */

package tilemaster.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class to read XML documents from files and streams.
 * 
 * @author Hj. Malthaner
 */
public class XmlReader
{
    /**
     * Read UTF-8 encoded XML data from a file.
     *
     * @param file The file to read.
     * @return A string with the XML data.
     *
     * @throws IOException in case of IO problems.
     */
    public String readFromFile(final File file) throws IOException
    {
        if(file == null)
        {
            throw new IOException("File must not be null.");
        }

        final FileInputStream inputStream = new FileInputStream(file);
        final String xml = readFromStream(inputStream);
        
        inputStream.close();
        
        return xml;
    }

    /**
     * Read UTF-8 encoded XML data from an input stream.
     * This method will close the input stream on EOF.
     *
     * @param stream The stream to read from.
     * @return A string with the XML data.
     *
     * @throws IOException in case of IO problems.
     */
    public String readFromStream(final InputStream stream) throws IOException
    {
        if(stream == null)
        {
            throw new IOException("Stream must not be null.");
        }

        final InputStreamReader streamReader = new InputStreamReader(stream, "UTF-8");
        final BufferedReader reader = new BufferedReader(streamReader);
        final String lineSeparator = System.getProperty("line.separator");
        final StringBuilder buffer = new StringBuilder();

        String line;
        while((line = reader.readLine()) != null)
        {
          buffer.append(line).append(lineSeparator);
        }

        reader.close();

        return buffer.toString();
    }

    /**
     * Create a document from a String which contains XML.
     *
     * @param xml The XML String input.
     * @return The created Document object or null in case of errors.
     *
     * @throws IOException in case of IO problems.
     */
    public Document buildDocument(final String xml) throws IOException
    {
        if(xml == null)
        {
            throw new IOException("XML string must not be null.");
        }

        Document result = null;

        try
        {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final StringReader reader = new StringReader(xml);
            final InputSource source = new InputSource(reader);

            result = builder.parse(source);
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (SAXException e)
        {
            e.printStackTrace();
        }

        return result;
    }
}
