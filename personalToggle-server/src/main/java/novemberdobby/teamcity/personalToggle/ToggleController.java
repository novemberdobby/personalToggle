package novemberdobby.teamcity.personalToggle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager; //closed API
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.jetbrains.annotations.NotNull;

public class ToggleController extends BaseController {
    
    private SBuildServer m_server;
    private AgentPoolManager m_poolManager;
    private ReentrantLock m_lock = new ReentrantLock();

    private Map<Integer, ToggleSetting> m_agents = new HashMap<Integer, ToggleSetting>();
    private Map<Integer, ToggleSetting> m_pools = new HashMap<Integer, ToggleSetting>();
    
    public ToggleController(@NotNull final SBuildServer server, @NotNull final WebControllerManager webManager, @NotNull AgentPoolManager poolManager) {
        m_server = server;
        m_poolManager = poolManager;
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
            catch(NumberFormatException ex) { }

            //fun fact, -1 is a valid pool (AgentPool.UNLIMITED), but we're not expecting that
            if(id < 0) {
                return null;
            }
            
            String setting = request.getParameter("setting");

            if("true".equals(request.getParameter("isPool"))) {
                if(m_poolManager.findAgentPoolById(id) == null) {
                    return null;
                }
                
                updateSetting(m_pools, id, user, setting);
            } else {
                if(m_server.getBuildAgentManager().findAgentById(id, true) == null) {
                    return null;
                }

                updateSetting(m_agents, id, user, setting);
            }
        }
        
        return null;
    }

    private void updateSetting(Map<Integer, ToggleSetting> map, Integer id, User user, String setting) {
        
        m_lock.lock();
        try {
            if("unset".equals(setting)) {
                map.remove(id);
            } else {
                map.put(id, ToggleSetting.valueOf(setting));
            }

            Loggers.SERVER.info(String.format("User %s set %s '%d' to '%s' for personal builds", user.getUsername(), map == m_agents ? "agent" : "pool", id, setting));
            save();
        }
        catch(IllegalArgumentException ex) {
            logger.info("Bad setting value for personal builds toggle: " + setting);
        }
        finally {
            m_lock.unlock();
        }
    }

    public Map<Integer, ToggleSetting> getAgentSettings() {
        m_lock.lock();
        
        try {
            return new HashMap<Integer, ToggleSetting>(m_agents);
        }
        finally {
            m_lock.unlock();
        }
    }
    
    public Map<Integer, ToggleSetting> getPoolSettings() {
        m_lock.lock();
        
        try {
            return new HashMap<Integer, ToggleSetting>(m_pools);
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