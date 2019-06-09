package novemberdobby.teamcity.personalToggle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.intellij.openapi.util.Pair;

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

        if(m_controller.isDisabled()) {
            AgentsFilterResult any = new AgentsFilterResult();
            any.setFilteredConnectedAgents(new ArrayList<SBuildAgent>(context.getAgentsForStartingBuild()));
            return any;
        }

        String buildId = context.getStartingBuild().getItemId();
        SQueuedBuild build = m_server.getQueue().findQueued(buildId);

        //sometimes this can be undefined, race condition possibly?
        if(build == null) {
            AgentsFilterResult empty = new AgentsFilterResult();
            empty.setWaitReason(new WaitReason() {
                @Override
                public String getDescription() {
                    return "Unknown build";
                }
            });
            return empty;
        }

        boolean isPersonal = build.isPersonal();

        Collection<SBuildAgent> defaultAgents = context.getAgentsForStartingBuild();
        List<SBuildAgent> possibleAgents = new ArrayList<SBuildAgent>(defaultAgents.size());
        Map<Integer, ToggleSetting> agentSettings = m_controller.getAgentSettings();
        Map<Integer, ToggleSetting> poolSettings = m_controller.getPoolSettings();

        for (SBuildAgent agent : defaultAgents) {
            ToggleSetting setting = agentSettings.getOrDefault(agent.getId(), ToggleSetting.Unset);

            //fall back to pool setting
            if(setting == ToggleSetting.Unset) {
                setting = poolSettings.getOrDefault(agent.getAgentPool().getAgentPoolId(), ToggleSetting.Unset);
            }

            if(setting == ToggleSetting.Unset
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

    /**
     * Return a pair with the count of personal & non-personal build runners respectively
     */
    public Pair<Integer, Integer> getCanRunCounts() {

        Map<Integer, ToggleSetting> agentSettings = m_controller.getAgentSettings();
        Map<Integer, ToggleSetting> poolSettings = m_controller.getPoolSettings();

        List<SBuildAgent> agents = m_server.getBuildAgentManager().getRegisteredAgents();

        Integer personal = 0;
        Integer nonPersonal = 0;

        for (SBuildAgent agent : agents) {
            if(!agent.isEnabled()) {
                continue;
            }
            
            ToggleSetting ts;
            
            if(m_controller.isDisabled()) {
                ts = ToggleSetting.Unset;
            } else {
                ts = agentSettings.getOrDefault(agent.getId(), ToggleSetting.Unset);
                if(ts == ToggleSetting.Unset) {
                    ts = poolSettings.getOrDefault(agent.getAgentPoolId(), ToggleSetting.Unset);
                }
            }

            switch(ts) {
                case Unset:
                    personal++;
                    nonPersonal++;
                    break;
                    
                case Only:
                    personal++;
                    break;
                
                case Never:
                    nonPersonal++;
                    break;
            }
        }

        return Pair.create(personal, nonPersonal);
    }
}