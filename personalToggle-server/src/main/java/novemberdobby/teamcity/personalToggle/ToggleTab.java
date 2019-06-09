package novemberdobby.teamcity.personalToggle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.agentPools.AgentPool;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager; //closed API
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.util.SessionUser;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

public class ToggleTab extends SimpleCustomTab {

    private SBuildServer m_server;
    private AgentPoolManager m_poolManager;
    private ToggleController m_status;
    
    public ToggleTab(@NotNull final PagePlaces places,
                        @NotNull final PluginDescriptor descriptor,
                        @NotNull final SBuildServer server,
                        @NotNull final ToggleController status,
                        @NotNull final AgentPoolManager poolManager) {
        super(places, PlaceId.AGENTS_TAB, descriptor.getPluginName(), "toggle_list.jsp", "Personal Builds"); //TODO show enabled count
        m_server = server;
        m_poolManager = poolManager;
        m_status = status;
        register();
    }
    
    @Override
    public boolean isAvailable(@NotNull HttpServletRequest request) {
        SUser user = SessionUser.getUser(request);
        //we could check what each agent can run, then check if the user has <manage agent> perms for that project, but eh
        return user != null && user.isPermissionGrantedGlobally(Permission.ADMINISTER_AGENT);
    }
    
    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        
        //show settings for agents and pools
        Map<SBuildAgent, String> agentsStatus = new HashMap<SBuildAgent, String>();
        Map<AgentPool, String> poolsStatus = new HashMap<AgentPool, String>();

        List<SBuildAgent> allAgents = new ArrayList<SBuildAgent>(m_server.getBuildAgentManager().getRegisteredAgents());
        allAgents.addAll(m_server.getBuildAgentManager().getUnregisteredAgents());

        Map<Integer, ToggleSetting> agentSettings = m_status.getAgentSettings();
        for(SBuildAgent agent : allAgents) {
            agentsStatus.put(agent, agentSettings.getOrDefault(agent.getId(), ToggleSetting.Unset).toString());
        }
        
        List<AgentPool> allPools = m_poolManager.getAllAgentPools();
        Map<Integer, ToggleSetting> poolSettings = m_status.getPoolSettings();
        for(AgentPool pool : allPools) {
            poolsStatus.put(pool, poolSettings.getOrDefault(pool.getAgentPoolId(), ToggleSetting.Unset).toString());
        }
        
        model.put("agents", agentsStatus);
        model.put("pools", poolsStatus);
    }
}
