package novemberdobby.teamcity.personalToggle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SQueuedBuild;
import jetbrains.buildServer.serverSide.buildDistribution.AgentsFilterContext;
import jetbrains.buildServer.serverSide.buildDistribution.AgentsFilterResult;
import jetbrains.buildServer.serverSide.buildDistribution.StartingBuildAgentsFilter;
import jetbrains.buildServer.serverSide.buildDistribution.WaitReason;

public class AgentsFilter implements StartingBuildAgentsFilter {

    ToggleController m_controller;
    SBuildServer m_server;

    public AgentsFilter(ToggleController agentsStatus, SBuildServer server) {
        m_controller = agentsStatus;
        m_server = server;
    }

    @Override
    public AgentsFilterResult filterAgents(AgentsFilterContext context) {

        String buildId = context.getStartingBuild().getItemId();
        SQueuedBuild build = m_server.getQueue().findQueued(buildId);
        boolean isPersonal = build.isPersonal();

        Collection<SBuildAgent> defaultAgents = context.getAgentsForStartingBuild();
        List<SBuildAgent> possibleAgents = new ArrayList<SBuildAgent>(defaultAgents.size());
        Map<Integer, ToggleSetting> settings = m_controller.getSettings();

        for (SBuildAgent agent : defaultAgents) {
            ToggleSetting setting = settings.getOrDefault(agent.getId(), ToggleSetting.Default);

            if(setting == ToggleSetting.Default
            || (isPersonal && setting != ToggleSetting.Never)
            || (!isPersonal && setting != ToggleSetting.Only)) {
                possibleAgents.add(agent);
            }
        }

        AgentsFilterResult result = new AgentsFilterResult();
        result.setFilteredConnectedAgents(possibleAgents);
        
        if(possibleAgents.isEmpty()) {
            result.setWaitReason(new WaitReason() {
                @Override
                public String getDescription() {
                    return "Cannot start due to available agent personal builds settings";
                }
            });
        }

        return result;
    }

}