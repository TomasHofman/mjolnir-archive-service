package org.jboss.set.mjolnir.archive.scheduler;


import org.jboss.logging.Logger;
import org.jboss.set.mjolnir.archive.ldap.LdapScanningBean;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Arrays;

@Singleton
public class JobScheduler {

    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    private LdapScanningBean ldapScanningBean;


    @Schedule(hour = "3", persistent = false)
    public void ldapScan() {
        logger.infof("Starting scheduled job ldapScan");
        ldapScanningBean.createRemovalsForUsersWithoutLdapAccount();
    }

    // TODO: remove this
    @Schedule(hour = "*", minute = "*/10", persistent = false)
    public void queryGitHubApi() throws IOException {
        InetAddress[] addresses = InetAddress.getAllByName("api.github.com");
        logger.infof("Resolving api.github.com to %s", Arrays.toString(addresses));
        HttpURLConnection connection = (HttpURLConnection) new URL("https://api.github.com").openConnection();
        connection.setConnectTimeout(10000);
        logger.infof("HTTP request to api.github.com: %d", connection.getResponseCode());
    }
}
