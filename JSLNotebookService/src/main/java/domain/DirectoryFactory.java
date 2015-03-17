package domain;

import dino.api.Directory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * Created by Jay on 3/12/2015.
 */
public class DirectoryFactory {

    private String jndiHostPort;

    public String getJndiHostPort() {
        return jndiHostPort;
    }

    public void setJndiHostPort(String jndiHostPort) {
        this.jndiHostPort = jndiHostPort;
    }

    public Directory Create() throws NamingException {

        InitialContext initialContext = null;
        if(jndiHostPort == null) {
            initialContext = new InitialContext();
        } else {
            String port = new HostAndPort(jndiHostPort).getPort();
            String host = new HostAndPort(jndiHostPort).getHost();

            Properties properties = new Properties();
            properties.setProperty("org.omg.CORBA.ORBInitialHost", host);
            if (port != null) {
                properties.setProperty("org.omg.CORBA.ORBInitialPort", port);
            }

            initialContext = new InitialContext(properties);
        }

        Directory directory = (Directory) initialContext.lookup("dino.api.Directory");
        return directory;
    }

}
