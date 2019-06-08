package novemberdobby.teamcity.personalToggle;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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

public class ToggleController extends BaseController {
    
    private SBuildServer m_server;
    private Map<Integer, ToggleSetting> m_agents = new HashMap<Integer, ToggleSetting>();
    private ReentrantLock m_lock = new ReentrantLock();
    
    public ToggleController(@NotNull final SBuildServer server, @NotNull final WebControllerManager webManager) {
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
            
            String setting = request.getParameter("setting");            

            BuildAgentManager bam = m_server.getBuildAgentManager();
            SBuildAgent agent = bam.findAgentById(id, true);
            if(agent != null) {
                m_lock.lock();
                
                try {
                    if("default".equals(setting)) {
                        m_agents.remove(id);
                    } else {
                        m_agents.put(id, ToggleSetting.valueOf(setting));
                    }
    
                    Loggers.SERVER.info(String.format("User %s set agent '%s' to '%s' for personal builds", user.getUsername(), agent.getName(), setting));
                    save();
                }
                catch(IllegalArgumentException ex) {
                    logger.info("Bad setting value for personal builds toggle: " + setting);
                }
                finally {
                    m_lock.unlock();
                }
            }
        }
        
        return null;
    }

    public Map<Integer, ToggleSetting> getSettings() {
        m_lock.lock();
        
        try {
            return new HashMap<Integer, ToggleSetting>(m_agents);
        }
        finally {
            m_lock.unlock();
        }
    }

    //TODO hook serverstartup, should probably cull unknown ids too
    public void load() {
        
    }
    
    //TODO
    public void save() {
        
    }
}