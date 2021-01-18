package com.briehman.leadtimeanalyzer.entity;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue
    @Column(updatable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    protected Team() {
    }

    public Team(@NotNull String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    private void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Team team = (Team) o;
        return name.equals(team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Team{id=" + id + ", name='" + name + '\'' + '}';
    }
}
