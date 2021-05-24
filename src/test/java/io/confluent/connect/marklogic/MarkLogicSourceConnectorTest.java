package io.confluent.connect.marklogic;

import org.apache.kafka.common.config.ConfigValue;
import org.apache.kafka.connect.connector.ConnectorContext;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class MarkLogicSourceConnectorTest extends EasyMockSupport {

    private static final String SINGLE_TOPIC = "test";

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
        sourceProperties.put(MarkLogicSourceConfig.CONNECTION_HOST, MarkLogicSourceConfig.CONNECTION_HOST_DEFAULT);
        sourceProperties.put(MarkLogicSourceConfig.CONNECTION_PORT, MarkLogicSourceConfig.CONNECTION_PORT_DEFAULT);
        sourceProperties.put(MarkLogicSourceConfig.CONNECTION_USER, MarkLogicSourceConfig.CONNECTION_USER_DEFAULT);
        sourceProperties.put(MarkLogicSourceConfig.CONNECTION_PASSWORD, MarkLogicSourceConfig.CONNECTION_PASSWORD_DEFAULT);
    }

    @Test
    public void testConnectorConfigValidation() {
        replayAll();
        List<ConfigValue> configValues = connector.config().validate(sourceProperties);
        for (ConfigValue val : configValues) {
            assertEquals(0, val.errorMessages().size(), MessageFormat.format("Config property errors: {0}", val.errorMessages()));
        }
        verifyAll();
    }
}
