// EdgeInfo.java
package comfaas;

public class EdgeInfo {
    private int edgeId;
    private String ip;
    private int port;

    public EdgeInfo(int edgeId, String ip, int port) {
        this.edgeId = edgeId;
        this.ip = ip;
        this.port = port;
    }

    public int getEdgeId() {
        return edgeId;
    }

    public void setEdgeId(int edgeId) {
        this.edgeId = edgeId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
