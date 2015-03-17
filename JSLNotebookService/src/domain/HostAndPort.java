package domain;

/**
 * Created by Jay on 3/13/2015.
 */
public class HostAndPort {

    private String hostAndPort;

    public HostAndPort(String hostAndPort) {
        if (hostAndPort == null) {
            throw new IllegalArgumentException("hostAndPort");
        }
        this.hostAndPort = hostAndPort;
    }

    public String getHost() {
        String[] splitHostAndPort = hostAndPort.split(":");
        if (splitHostAndPort.length == 0) {
            return null;
        } else {
            return splitHostAndPort[0];
        }
    }

    public String getPort() {
        String[] splitHostAndPort = hostAndPort.split(":");
        if (splitHostAndPort.length < 2) {
            return null;
        } else {
            return splitHostAndPort[1];
        }
    }
}
