package io.confluent.connect.marklogic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationTest {

    @Test
    public void BasicConfigurationTest(){
        MarkLogicXccContentSourceProvider.generateXdbcConnectionUri("u", "p", "h", "8000");
    }

    @Test
    public void PortAsIntTest(){
        assertEquals(8000, Integer.parseInt("8000"));
    }

    @Test
    public void ConfigTest(){
        //System.out.println("i"+MarkLogicSource.config().toHtml());
    }
}
