package io.confluent.connect.marklogic;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkLogicConnectionTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void MarkLogicHealthCheckTest() {
        // Simplest possible test: an unauthenticated HTTP connection to the MarkLogic healthcheck probe to confirm MarkLogic is fully initialized and responding
        try {
            URL url = new URL(String.format("http://%s:7997", MarkLogicSourceConfig.CONNECTION_HOST_DEFAULT));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            LOG.debug(String.format("Healthcheck HTTP response: %s", con.getResponseMessage()));
            LOG.debug(String.format("Healthcheck HTTP response code: %d", con.getResponseCode()));
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String responseBody = br.lines().collect(Collectors.joining());
            LOG.debug(String.format("Healthcheck HTTP Response Body: %s", responseBody));
            assertEquals("<html><body>Healthy</body></html>", responseBody);
            assertEquals(200, con.getResponseCode());
            assertEquals("OK", con.getResponseMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ConnectionTest() {
        DatabaseClient client = DatabaseClientFactory.newClient(MarkLogicSourceConfig.CONNECTION_HOST_DEFAULT, Integer.parseInt(MarkLogicSourceConfig.CONNECTION_PORT_DEFAULT), "Meters",
                new DatabaseClientFactory.DigestAuthContext(MarkLogicSourceConfig.CONNECTION_USER_DEFAULT, MarkLogicSourceConfig.CONNECTION_PASSWORD_DEFAULT));
        assertEquals("Meters", client.getDatabase());
        assertTrue(client.checkConnection().isConnected());

        // Generate a full and-query (to get every URI)
        StructuredQueryDefinition sqd = new StructuredQueryBuilder().and();

        DataMovementManager dmm = client.newDataMovementManager();
        QueryBatcher batcher = dmm.newQueryBatcher(sqd);
        batcher.onUrisReady(batch -> {
                    assertEquals(StringUtils.left(String.valueOf(batch.getTimestamp().toInstant().getEpochSecond()), 4), StringUtils.left(String.valueOf(Instant.now().getEpochSecond()), 4));
                    LOG.debug(String.format("Server Timestamp: %s", batch.getTimestamp().toString()));
                    // NOTE - this always seems to be -1 and really shouldn't be (the query can't be in update mode, right?) - not a problem that we can deal with, but disappointing nonetheless: LOG.info("TS:"+batch.getServerTimestamp());
                    for (String uri : batch.getItems()) {
                        LOG.debug("URI: " + uri);
                    }
                }
        ).onQueryFailure(Throwable::printStackTrace);
        // *** Step 4: Submit the DMSDK job ***
        dmm.startJob(batcher);
        // Wait for the job to complete, and then stop it.
        batcher.awaitCompletion();
        dmm.stopJob(batcher);
    }
}
