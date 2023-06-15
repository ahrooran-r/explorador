package lk.uom.dc.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

public class Base {

    private static final ObjectMapper jackson;

    static {
        jackson = YAMLMapper
                .builder()
                .addModule(new AfterburnerModule())
                .build();
    }

    public static ObjectMapper getMapper() {
        return jackson;
    }
}
