package org.submarine.builder;

import java.util.Objects;

public class QuarkusKnativeBuildRequest {

    private String repo;

    private String branch;

    private String workDir;

    public QuarkusKnativeBuildRequest() {
    }

    public QuarkusKnativeBuildRequest(String repo, String branch, String workDir) {
        this.repo = repo;
        this.branch = branch;
        this.workDir = workDir;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof QuarkusKnativeBuildRequest)) {
            return false;
        }

        QuarkusKnativeBuildRequest other = (QuarkusKnativeBuildRequest) obj;

        return Objects.equals(other.repo, this.repo)
                && Objects.equals(other.branch, this.branch)
                && Objects.equals(other.workDir, this.workDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.repo, this.branch, this.workDir);
    }
}
