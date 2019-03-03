package novemberdobby.teamcity.personalToggle;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.SBuildAgent;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.jetbrains.annotations.NotNull;

public class ToggleStatus extends BaseController {
    
    private SBuildServer m_server;
    private Set<Integer> m_disabled = new HashSet<Integer>();
    private ReentrantLock m_lock = new ReentrantLock();
    
    public ToggleStatus(@NotNull final SBuildServer server, @NotNull final WebControllerManager webManager) {
        m_server = server;
        webManager.registerController(Constants.TOGGLE_URL, this);
    }
    
    @Override
    protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
        
        SUser user = SessionUser.getUser(request);
        if(user.isPermissionGrantedGlobally(Permission.ADMINISTER_AGENT)) {
            
            int id = -1;
            
            try {
                id = Integer.parseInt(request.getParameter("id"));
            }
            catch(Exception ex) {
                return null;
            }
            
            boolean enabled = "true".equals(request.getParameter("enabled"));
            
            BuildAgentManager bam = m_server.getBuildAgentManager();
            SBuildAgent agent = bam.findAgentById(id, true);
            if(agent != null) {
                m_lock.lock();
                
                try {
                    if(enabled) {
                        m_disabled.remove(id);
                    } else {
                        m_disabled.add(id);
                    }
    
                    Loggers.SERVER.info(String.format("User %s %s agent '%s' for personal builds", user.getUsername(), enabled ? "enabled" : "disabled", agent.getName()));
                    save();
                }
                finally {
                    m_lock.unlock();
                }
            }
        }
        
        return null;
    }

    public boolean getIsEnabled(int agentId) {
        m_lock.lock();
        try {
            return !m_disabled.contains(agentId);
        }
        finally {
            m_lock.unlock();
        }
    }

    //TODO: only allow personal builds/don't allow personal builds/default
    
    //TODO hook serverstartup, should probably cull unknown ids too
    public void load() {
        
    }
    
    //TODO
    public void save() {
        
    }
}