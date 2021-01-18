package com.briehman.leadtimeanalyzer.service;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.briehman.leadtimeanalyzer.entity.AbstractCommit;
import com.briehman.leadtimeanalyzer.entity.CodeRepository;
import com.briehman.leadtimeanalyzer.entity.Merge;
import com.briehman.leadtimeanalyzer.entity.Team;
import com.briehman.leadtimeanalyzer.repository.MergeRepository;
import com.briehman.leadtimeanalyzer.repository.TeamRepository;
import com.google.common.collect.Lists;
import io.atlassian.util.concurrent.Promise;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Assigns teams to individual tickets based upon a JIRA custom field. The field
 * is assumed to be a custom field whose values are the names of the teams.
 */
@Service
public class TeamAssigner {

    private static final Logger LOG = LoggerFactory.getLogger(TeamAssigner.class);

    public static final int PARTITION_SIZE = 500;

    private final TeamRepository teamRepository;
    private final JiraRestClient restClient;
    private final MergeRepository mergeRepository;
    private final String assignedTeamFieldId;

    @Autowired
    public TeamAssigner(TeamRepository teamRepository, JiraRestClient restClient,
            MergeRepository mergeRepository,
            @Value("${jira.assigned_team_custom_field}") String assignedTeamFieldId) {
        this.teamRepository = teamRepository;
        this.restClient = restClient;
        this.mergeRepository = mergeRepository;
        this.assignedTeamFieldId = assignedTeamFieldId;
    }

    public void assignTeams(CodeRepository repo) {
        List<Merge> noTeamMerges = mergeRepository.findAllByRepositoryAndTeamIsNull(repo);

        if (noTeamMerges.isEmpty()) {
            return;
        }

        for (List<Merge> group : Lists.partition(noTeamMerges, PARTITION_SIZE)) {
            String keys = group.stream()
                    .map(AbstractCommit::getTicket)
                    .collect(Collectors.joining(", "));

            Map<String, Merge> mergesByTicket = group.stream()
                    .collect(Collectors.toMap(Merge::getTicket, m -> m, (a, b) -> a));

            Set<String> searchFields = Set
                    .of("key", assignedTeamFieldId, "summary", "issuetype", "created",
                            "updated", "project", "status");
            Promise<SearchResult> searchResultPromise = restClient.getSearchClient()
                    .searchJql("key IN (" + keys + ")", PARTITION_SIZE, 0, searchFields);

            for (Issue issue : searchResultPromise.claim().getIssues()) {
                IssueField assignedTeamField = issue.getField(assignedTeamFieldId);
                if (assignedTeamField != null && assignedTeamField.getValue() != null) {
                    JSONObject assignedTeamObject = (JSONObject) assignedTeamField.getValue();
                    String assignedTeamName;
                    try {
                        assignedTeamName = assignedTeamObject.getString("value");
                    } catch (JSONException e) {
                        LOG.error(e.getMessage(), e);
                        continue;
                    }

                    Team assignedTeam = teamRepository.findByName(assignedTeamName)
                            .orElseGet(() -> {
                                Team team = new Team(assignedTeamName);
                                teamRepository.save(team);
                                return team;
                            });

                    Merge merge = mergesByTicket.get(issue.getKey());
                    if (merge == null) {
                        LOG.warn("Unable to find merge for ticket " + issue.getKey());
                    } else {
                        merge.setTeam(assignedTeam);
                        mergeRepository.save(merge);
                    }
                }
            }
        }
    }
}
