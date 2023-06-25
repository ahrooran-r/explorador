//package lk.uom.dc.model;
//
//import lombok.AllArgsConstructor;
//
//import java.util.Arrays;
//
//@AllArgsConstructor
//public enum Token {
//
//    // Header commands
//    REG("REG", "register request"),
//    UNREG("UNREG", "un register request"),
//    REGOK("UNREG", "registration success"),
//
//    // length commands
//    NO_NODES("0", "request is successful, no nodes in the system"),
//    ONE("1", "request is successful, 1 node's contacts will be returned"),
//    TWO("2", "request is successful, 1 or 2 nodes' contacts will be returned"),
//    FAILED("9999", "failed, there is some error in the command"),
//    ALREADY_REGISTERED("9998", "already registered to you, unregister first"),
//    PORT_OCCUPIED("9997", "failed, registered to another user, try a different IP and port"),
//    BS_FULL("9996", "failed, can’t register. BS full");
//
//    public final String id;
//    public final String description;
//
//    public static Token value(String command) {
//        return Arrays.stream(Token.values())
//                .filter(c -> c.id.equalsIgnoreCase(command))
//                .findFirst()
//                .orElseThrow();
//    }
//}
