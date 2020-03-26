package org.jboss.mjolnir.archive.service.webapp.servlet;

import org.jboss.logging.Logger;
import org.jboss.set.mjolnir.archive.ldap.LdapScanningBean;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

@WebServlet("/ldap-scan")
public class LdapScanningServlet extends HttpServlet {

    private final Logger logger = Logger.getLogger(getClass());

    @Inject
    private LdapScanningBean ldapScanningBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InetAddress address = InetAddress.getByName("api.github.com");
        logger.infof("Resolving api.github.com to %s", address);
        ldapScanningBean.createRemovalsForUsersWithoutLdapAccount();

        resp.setContentType("text/plain");
        ServletOutputStream os = resp.getOutputStream();
        os.println("OK");
    }
}
