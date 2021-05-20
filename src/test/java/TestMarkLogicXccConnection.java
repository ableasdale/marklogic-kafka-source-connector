import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import io.confluent.connect.marklogic.MarkLogicXccContentSourceProvider;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class TestMarkLogicXccConnection {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Test
    public void ConnectionTest(){

        Session s = MarkLogicXccContentSourceProvider.getSession("Meters");
        try {
            LOG.info("Server Point in Time: "+ s.getCurrentServerPointInTime());
            System.out.println("Server Point in Time: "+ s.getCurrentServerPointInTime());
        } catch (RequestException e) {
            LOG.error("Exception: ",e);
        }
    }
}
