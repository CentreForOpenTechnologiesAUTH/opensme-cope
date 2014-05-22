package eu.opensme.cope.knowledgemanager.xml;

import eu.opensme.cope.knowledgemanager.gui.management.Management;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class CompareSynchronizer {

    private static final String XML = "./compare_sync.xml";
    private XMLRepository xmlRepository;

    public CompareSynchronizer() {
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(XMLRepository.class);
        } catch (JAXBException ex) {
            Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!new File(XML).exists()) {
            Writer w = null;
            try {
                w = new FileWriter(XML);
                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                xmlRepository = new XMLRepository();
                m.marshal(xmlRepository, w);
            } catch (JAXBException ex) {
                Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    w.close();
                } catch (IOException ex) {
                    Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            try {
                Unmarshaller um = context.createUnmarshaller();
                xmlRepository = (XMLRepository) um.unmarshal(new FileReader(XML));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JAXBException ex) {
                Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void saveRepository() {
        Writer w = null;
        try {
            w = new FileWriter(XML);
            JAXBContext context = JAXBContext.newInstance(XMLRepository.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            m.marshal(xmlRepository, w);
            Management.jLabelSync.setForeground(Color.RED);
            Management.jLabelSync.setText("The semantic repository is not synchronized with COMPARE. Click here to synchronize");
        } catch (JAXBException ex) {
            Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                w.close();
            } catch (IOException ex) {
                Logger.getLogger(CompareSynchronizer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void clearRepository() {
        xmlRepository.clear();
        saveRepository();
        Management.jLabelSync.setForeground(Color.BLUE);
        Management.jLabelSync.setText("The semantic repository is synchronized with COMPARE");
    }

    public void setComponentStatus(String id, SYNC status) {
        xmlRepository.addComponent(id, status);
        saveRepository();
    }

    public boolean isSynchronized() {
        if (xmlRepository.getComponentList().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public XMLRepository getRepository() {
        return xmlRepository;
    }
}
