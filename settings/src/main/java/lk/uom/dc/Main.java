package lk.uom.dc;

import lk.uom.dc.settings.LogConfig;

public class Main {
    public static void main(String[] args) {

        var v1 = LogConfig.get("app", "file").asText();
        var v2 = LogConfig.get("app", "enabled").asBoolean();
        var v3 = LogConfig.get("app", "console").asBoolean();

        System.out.println(v1);
        System.out.println(v2);
        System.out.println(v3);

    }
}
