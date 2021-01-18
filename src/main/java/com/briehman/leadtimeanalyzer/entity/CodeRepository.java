package com.briehman.leadtimeanalyzer.entity;

import java.io.File;
import java.io.IOException;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

@Entity
@Table(name = "repositories")
public class CodeRepository {

    @Id
    @GeneratedValue
    @Column(updatable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String primaryBranch;

    @Column(nullable = false, name = "uses_teams")
    private Boolean usesTeams;

    @Transient
    private Repository repo;

    public CodeRepository() {
    }

    public CodeRepository(String name, String filePath, String primaryBranch, boolean usesTeams) {
        this.name = name;
        this.filePath = filePath;
        this.primaryBranch = primaryBranch;
        this.usesTeams = usesTeams;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getPrimaryBranch() {
        return primaryBranch;
    }

    public void setPrimaryBranch(String primaryBranch) {
        this.primaryBranch = primaryBranch;
    }

    public Boolean getUsesTeams() {
        return usesTeams;
    }

    public void setUsesTeams(Boolean usesTeams) {
        this.usesTeams = usesTeams;
    }

    public Repository getGitRepository() throws IOException {
        if (repo == null) {
            synchronized (this) {
                if (repo == null) {
                    repo = getRepository(filePath);
                }
            }
        }
        return repo;
    }

    private static Repository getRepository(String pathname) throws IOException {
        FileRepositoryBuilder fileRepositoryBuilder = new FileRepositoryBuilder();
        fileRepositoryBuilder.setGitDir(new File(pathname));
        fileRepositoryBuilder.setMustExist(true);
        return fileRepositoryBuilder.build();
    }
}
