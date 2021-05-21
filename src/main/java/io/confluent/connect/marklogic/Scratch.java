package io.confluent.connect.marklogic;

import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.datamovement.DataMovementManager;
import com.marklogic.client.datamovement.QueryBatcher;
import com.marklogic.client.query.StructuredQueryBuilder;
import com.marklogic.client.query.StructuredQueryDefinition;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.exceptions.XccConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URI;

public class Scratch {
    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static void main(String[] args) {

        // host.docker.internal
        try {
            ContentSource cs = ContentSourceFactory.newContentSource(URI.create("xcc://admin:admin@localhost:8000/Meters"));
            Session s = cs.newSession();
            LOG.info("MarkLogicSourceTask: current MarkLogic Timestamp: "+s.getCurrentServerPointInTime());
        } catch (RequestException | XccConfigException e) {
            LOG.info("MarkLogicSourceTask: Exception Caught: ",e);
        }

        LOG.debug("here");

        DatabaseClient client = DatabaseClientFactory.newClient("localhost", 8000, "Meters",
                new DatabaseClientFactory.DigestAuthContext("admin", "admin"));


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
