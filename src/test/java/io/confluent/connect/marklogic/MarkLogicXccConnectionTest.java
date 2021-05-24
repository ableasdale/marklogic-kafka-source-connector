package io.confluent.connect.marklogic;

import com.marklogic.xcc.AdhocQuery;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import io.confluent.connect.marklogic.MarkLogicXccContentSourceProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkLogicXccConnectionTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void XccConnectionTest(){
        Session s = MarkLogicXccContentSourceProvider.getSession("Meters");
        try {
            LOG.debug(MessageFormat.format("Server Point in Time: {0}", s.getCurrentServerPointInTime()));
            assertTrue(s.getCurrentServerPointInTime().toString().startsWith("1"));
            assertEquals("Meters", s.getContentBaseName());
            Request r = s.newAdhocQuery("cts:uris()");
            ResultSequence rs = s.submitRequest(r);
            assertTrue(rs.size() > 1);
           // LOG.debug(rs.asString());
            s.close();
            assertTrue(s.getConnectionUri().toString().endsWith(":8000/Meters"));
        } catch (RequestException e) {
            LOG.error("Exception: ",e);
        }
    }
}
