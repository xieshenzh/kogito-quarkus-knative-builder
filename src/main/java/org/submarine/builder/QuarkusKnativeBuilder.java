package org.submarine.builder;

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
            "apiVersion: serving.knative.dev/v1alpha1\n" +
            "kind: Service\n" +
            "metadata:\n" +
            "  name: ${service}\n" +
            "spec:\n" +
            "  runLatest:\n" +
            "    configuration:\n" +
            "      build:\n" +
            "        apiVersion: build.knative.dev/v1alpha1\n" +
            "        kind: Build\n" +
            "        spec:\n" +
            "          serviceAccountName: ${serviceAccount}\n" +
            "          source:\n" +
            "            git:\n" +
            "              revision: ${branch}\n" +
            "              url: ${repo}\n" +
            "          template:\n" +
            "            arguments:\n" +
            "              - name: IMAGE\n" +
            "                value: ${registryAccount}/${service}:${tag}\n" +
            "              - name: CONTEXT_DIR\n" +
            "                value: /workspace/${workDir}\n" +
            "              - name: SUBMARINE_MAVEN_MIRROR\n" +
            "                value: ${mavenMirror}\n" +
            "            name: ${buildTemplate}\n" +
            "          timeout: 20m\n" +
            "      revisionTemplate:\n" +
            "        metadata:\n" +
            "          labels:\n" +
            "            app: ${service}\n" +
            "        spec:\n" +
            "          container:\n" +
            "            image: ${registryAccount}/${service}:${tag}";

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
                                             QuarkusKnativeBuildRequest request) {

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

        return new QuarkusKnativeBuildRequest(request.getRepo(), request.getBranch(), serviceYaml);
    }
}