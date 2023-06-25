package lk.uom.dc;

import lk.uom.dc.settings.Log;

public class Main {
    public static void main(String[] args) {

        var v1 = Log.get("app", "file").asText();
        var v2 = Log.get("app", "enabled").asBoolean();
        var v3 = Log.get("app", "console").asBoolean();

        System.out.println(v1);
        System.out.println(v2);
        System.out.println(v3);

    }
}
