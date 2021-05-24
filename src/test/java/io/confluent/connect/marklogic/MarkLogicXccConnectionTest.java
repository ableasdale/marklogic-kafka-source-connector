package io.confluent.connect.marklogic;

import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkLogicXccConnectionTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void XccConnectionTest() {
        Session s = MarkLogicXccContentSourceProvider.getSession("Meters");
        try {
            LOG.debug(MessageFormat.format("Server Point in Time: {0}", s.getCurrentServerPointInTime()));
            assertEquals(StringUtils.left(s.getCurrentServerPointInTime().toString(), 4), StringUtils.left(String.valueOf(Instant.now().getEpochSecond()), 4));
            assertEquals("Meters", s.getContentBaseName());
            Request r = s.newAdhocQuery("cts:uris()");
            ResultSequence rs = s.submitRequest(r);
            assertTrue(rs.size() > 1);
            // LOG.debug(rs.asString());
            s.close();
            assertTrue(s.getConnectionUri().toString().endsWith(":8000/Meters"));
        } catch (RequestException e) {
            LOG.error("Exception: ", e);
        }
    }
}
