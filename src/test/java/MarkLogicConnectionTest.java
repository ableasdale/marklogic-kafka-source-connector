import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MarkLogicConnectionTest {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void MarkLogicHealthCheckTest(){
        // Simplest possible test: an unauthenticated HTTP connection to the MarkLogic healthcheck probe to confirm MarkLogic is fully initialized and responding
        try {
            URL url = new URL("http://localhost:7997");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            LOG.info("Healthcheck HTTP response: "+con.getResponseMessage());
            LOG.info("Healthcheck HTTP response code: "+con.getResponseCode());

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String responseBody = br.lines().collect(Collectors.joining());
            LOG.info("Healthcheck HTTP Response Body: "+ responseBody);
            assertEquals("<html><body>Healthy</body></html>", responseBody);
            assertEquals(200, con.getResponseCode());
            assertEquals("OK", con.getResponseMessage());
        } catch (ProtocolException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void ConnectionTest(){
        LOG.debug("here");
        assertTrue(true);
        DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8000, "Meters",
                new DatabaseClientFactory.DigestAuthContext("admin", "admin"));

        assertEquals("Meters", client.getDatabase());
        LOG.info("*** MARKLOGIC SOURCE CONNECTOR :: Client created: "+client.getDatabase());

        // Generate a full and-query (to get every URI)
        StructuredQueryDefinition sqd = new StructuredQueryBuilder().and();

        DataMovementManager dmm = client.newDataMovementManager();
        QueryBatcher batcher = dmm.newQueryBatcher(sqd);
        batcher.onUrisReady(batch -> {
                    for (String uri : batch.getItems()) {
                        LOG.info("URI: " + uri);
                    }
                }
        )
                .onQueryFailure(exception -> exception.printStackTrace());
        // *** Step 4: Submit the DMSDK job ***
        dmm.startJob(batcher);
        // Wait for the job to complete, and then stop it.
        batcher.awaitCompletion();
        dmm.stopJob(batcher);

    }
}
