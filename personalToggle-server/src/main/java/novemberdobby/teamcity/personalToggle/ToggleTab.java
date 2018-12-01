package novemberdobby.teamcity.personalToggle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;

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
    
    public ToggleTab(@NotNull final PagePlaces places,
                        @NotNull final PluginDescriptor descriptor,
                        @NotNull final SBuildServer server) {
        super(places, PlaceId.AGENTS_TAB, descriptor.getPluginName(), "toggle_list.jsp", "Personal Builds"); //TODO show count enabled
        m_server = server;
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
        
        List<SBuildAgent> agents = new ArrayList<SBuildAgent>(m_server.getBuildAgentManager().getRegisteredAgents());
        agents.addAll(m_server.getBuildAgentManager().getUnregisteredAgents());
        
        model.put("agents", agents);
    }
}
