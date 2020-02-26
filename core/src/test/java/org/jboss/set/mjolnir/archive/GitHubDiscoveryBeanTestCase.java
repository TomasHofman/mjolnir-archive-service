package org.jboss.set.mjolnir.archive;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.jboss.set.mjolnir.archive.TestUtils.readSampleResponse;
import static org.junit.Assert.assertEquals;

public class GitHubDiscoveryBeanTestCase {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

    private GitHubClient client = new GitHubClient("localhost", 8089, "http");

    @Before
    public void setup() throws IOException, URISyntaxException {
        stubFor(get(urlPathEqualTo("/api/v3/orgs/testorg/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readSampleResponse("responses/gh-orgs-repos-response.json"))));

        stubFor(get(urlPathMatching("/api/v3/repos/testorg/aphrodite/forks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readSampleResponse("responses/gh-repos-forks-aphrodite-response.json"))));

        stubFor(get(urlPathMatching("/api/v3/repos/testorg/activemq-artemis/forks"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readSampleResponse("responses/gh-repos-forks-artemis-response.json"))));
    }

    @Test
    public void testWiremockResponses() throws Exception {
        RepositoryService repositoryService = new RepositoryService(client);

        List<Repository> organizationRepositories = repositoryService.getOrgRepositories("testorg");
        assertEquals(3, organizationRepositories.size());
        assertThat(organizationRepositories)
                .extracting("owner.login", "name")
                .containsOnly(
                        tuple("testorg", "activemq-artemis"),
                        tuple("testorg", "aphrodite"),
                        tuple("testorg", "aesh"));

        List<Repository> forks = repositoryService.getForks(() -> "testorg/aphrodite");
        assertEquals(2, forks.size());
        assertThat(forks)
                .extracting("owner.login", "name")
                .containsOnly(
                        tuple("TomasHofman", "aphrodite"),
                        tuple("Belaran", "aphrodite"));

        forks = repositoryService.getForks(() -> "testorg/activemq-artemis");
        assertEquals(2, forks.size());
        assertThat(forks)
                .extracting("owner.login", "name")
                .containsOnly(
                        tuple("TomasHofman", "activemq-artemis"),
                        tuple("otheruser", "activemq-artemis"));
    }

    @Test
    public void testGetRepositoriesToArchive() throws Exception {
        GitHubDiscoveryBean bean = new GitHubDiscoveryBean(client);

        Set<Repository> privateRepositories = bean.getRepositoriesToArchive("testorg", "TomasHofman");
        assertEquals(2, privateRepositories.size());
        assertThat(privateRepositories)
                .extracting("owner.login", "name")
                .containsOnly(
                        tuple("TomasHofman", "aphrodite"),
                        tuple("TomasHofman", "activemq-artemis"));
    }

}