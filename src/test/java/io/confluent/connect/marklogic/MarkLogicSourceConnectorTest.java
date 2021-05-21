package io.confluent.connect.marklogic;

import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.connector.ConnectorContext;
import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MarkLogicSourceConnectorTest extends EasyMockSupport {

    private static final String SINGLE_TOPIC = "test";
    private static final String HOSTNAME = "marklogic";

    private MarkLogicSource connector;
    private ConnectorContext ctx;
    private Map<String, String> sourceProperties;

    @BeforeEach
    public void setup() {
        connector = new MarkLogicSource();
        ctx = createMock(ConnectorContext.class);
        connector.initialize(ctx);

        sourceProperties = new HashMap<>();
        sourceProperties.put(MarkLogicSourceConfig.TOPIC_CONFIG, SINGLE_TOPIC);
        sourceProperties.put(MarkLogicSourceConfig.CONNECTION_HOST, HOSTNAME);
    }

    @Test
    public void testConnectorConfigValidation() {
        replayAll();
        List<ConfigValue> configValues = connector.config().validate(sourceProperties);
        for (ConfigValue val : configValues) {
            assertEquals(0, val.errorMessages().size(), "Config property errors: " + val.errorMessages());
        }
        verifyAll();
    }
}
