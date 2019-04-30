package org.submarine.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/deploy")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuarkusKnativeBuilder {

    private final static String SERVICE_YAML =
            "{" +
            "  \"apiVersion\": \"serving.knative.dev/v1alpha1\"," +
            "  \"kind\": \"Service\"," +
            "  \"metadata\": {" +
            "    \"name\": \"${service}\"" +
            "  }," +
            "  \"spec\": {" +
            "    \"runLatest\": {" +
            "      \"configuration\": {" +
            "        \"build\": {" +
            "          \"apiVersion\": \"build.knative.dev/v1alpha1\"," +
            "          \"kind\": \"Build\"," +
            "          \"spec\": {" +
            "            \"serviceAccountName\": \"${serviceAccount}\"," +
            "            \"source\": {" +
            "              \"git\": {" +
            "                \"revision\": \"${branch}\"," +
            "                \"url\": \"${repo}\"" +
            "              }" +
            "            }," +
            "            \"template\": {" +
            "              \"arguments\": [" +
            "                {" +
            "                  \"name\": \"IMAGE\"," +
            "                  \"value\": \"${registryAccount}/${service}:${tag}\"" +
            "                }," +
            "                {" +
            "                  \"name\": \"CONTEXT_DIR\"," +
            "                  \"value\": \"/workspace/${workDir}\"" +
            "                }," +
            "                {" +
            "                  \"name\": \"SUBMARINE_MAVEN_MIRROR\"," +
            "                  \"value\": \"${mavenMirror}\"" +
            "                }" +
            "              ]," +
            "              \"name\": \"${buildTemplate}\"" +
            "            }," +
            "            \"timeout\": \"20m\"" +
            "          }" +
            "        }," +
            "        \"revisionTemplate\": {" +
            "          \"metadata\": {" +
            "            \"labels\": {" +
            "              \"app\": \"${service}\"" +
            "            }" +
            "          }," +
            "          \"spec\": {" +
            "            \"container\": {" +
            "              \"image\": \"${registryAccount}/${service}:${tag}\"" +
            "            }" +
            "          }" +
            "        }" +
            "      }" +
            "    }" +
            "  }" +
            "}";

    private String mavenMirror = System.getenv("MAVEN_MIRROR");
    private String buildTemplate = System.getenv("BUILD_TEMPLATE_NAME");
    private String serviceAccount = System.getenv("SERVICE_ACCOUNT_NAME");
    private String registryAccount = System.getenv("REGISTRY_ACCOUNT");

    {
        mavenMirror = mavenMirror == null ? "" : mavenMirror;
        buildTemplate = buildTemplate == null ? "" : buildTemplate;
        serviceAccount = serviceAccount == null ? "" : serviceAccount;
        registryAccount = registryAccount == null ? "" : registryAccount;
    }

    @POST
    @Path("{service}/{tag}")
    public QuarkusKnativeBuildRequest deploy(@PathParam("service") String service,
                                             @PathParam("tag") String tag,
                                             QuarkusKnativeBuildRequest request) throws IOException {

        String serviceYaml = SERVICE_YAML
                .replaceAll("\\$\\{service\\}", service)
                .replaceAll("\\$\\{tag\\}", tag)
                .replaceAll("\\$\\{repo\\}", request.getRepo())
                .replaceAll("\\$\\{branch\\}", request.getBranch())
                .replaceAll("\\$\\{workDir\\}", request.getWorkDir())
                .replaceAll("\\$\\{mavenMirror\\}", mavenMirror)
                .replaceAll("\\$\\{buildTemplate\\}", buildTemplate)
                .replaceAll("\\$\\{serviceAccount\\}", serviceAccount)
                .replaceAll("\\$\\{registryAccount\\}", registryAccount);

        String token = executeCommand(new String[]{"/bin/bash", "-c", "cat /var/run/secrets/kubernetes.io/serviceaccount/token"});
        String namespace = executeCommand(new String[]{"/bin/bash", "-c", "cat /var/run/secrets/kubernetes.io/serviceaccount/namespace"});

        return new QuarkusKnativeBuildRequest(namespace, token, serviceYaml);
    }

    private String executeCommand(String[] commands) throws IOException {
        Process process = Runtime.getRuntime().exec(commands);

        try (InputStream is = process.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.readLine();
        }
    }
}