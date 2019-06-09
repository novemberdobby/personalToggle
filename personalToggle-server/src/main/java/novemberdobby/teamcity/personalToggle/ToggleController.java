package novemberdobby.teamcity.personalToggle;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.BuildAgentManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager; //closed API
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.User;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import jetbrains.buildServer.web.util.SessionUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.springframework.web.servlet.ModelAndView;
import org.jetbrains.annotations.NotNull;

public class ToggleController extends BaseController {
    
    private SBuildServer m_server;
    private AgentPoolManager m_poolManager;
    private ServerPaths m_serverPaths;

    private ReentrantLock m_lock = new ReentrantLock();

    private Map<Integer, ToggleSetting> m_agents = new HashMap<Integer, ToggleSetting>();
    private Map<Integer, ToggleSetting> m_pools = new HashMap<Integer, ToggleSetting>();
    private Boolean m_disabled = false;

    public ToggleController(@NotNull final SBuildServer server,
            @NotNull final WebControllerManager webManager,
            @NotNull final AgentPoolManager poolManager,
            @NotNull final ServerPaths serverPaths) {
        m_server = server;
        m_poolManager = poolManager;
        m_serverPaths = serverPaths;
        webManager.registerController(Constants.TOGGLE_URL, this);
    }
    
    @Override
    protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
        
        SUser user = SessionUser.getUser(request);

        String toggle = request.getParameter("enabled");
        if(toggle != null && user.isSystemAdministratorRoleGranted()) {
            boolean disable = toggle.equals("false");
            if(m_disabled != disable) {
                m_disabled = disable;
                Loggers.SERVER.info(String.format("User %s %s personal builds filter", user.getUsername(), m_disabled ? "disabled" : "enabled"));
                save();
            }

        } else if(user.isPermissionGrantedGlobally(Permission.ADMINISTER_AGENT)) {
            
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
    
    public void load() {
        m_lock.lock();

        SaveData data = null;        
        try {
            Reader reader = new InputStreamReader(new FileInputStream(getSavePath()) , "UTF-8");
            Gson gson = new GsonBuilder().create();
            data = gson.fromJson(reader, SaveData.class);
            reader.close();
        }
        catch(Exception ex) {
            Loggers.SERVER.error("Failed to save personal builds toggle data: " + ex.toString());
        }
        finally {
            if(data != null) {
                m_disabled = data.Disabled;

                //cull any old IDs that don't exist any more
                m_agents.clear();
                BuildAgentManager bma = m_server.getBuildAgentManager();
                for (Entry<Integer, ToggleSetting> a : data.Agents.entrySet()) {
                    if(bma.findAgentById(a.getKey(), true) != null) {
                        m_agents.put(a.getKey(), a.getValue());
                    } else {
                        Loggers.SERVER.info("Personal build toggle: unknown agent id " + a.getKey());
                    }
                }

                m_pools.clear();
                for (Entry<Integer, ToggleSetting> a : data.Pools.entrySet()) {
                    if(m_poolManager.findAgentPoolById(a.getKey()) != null) {
                        m_pools.put(a.getKey(), a.getValue());
                    } else {
                        Loggers.SERVER.info("Personal build toggle: unknown pool id " + a.getKey());
                    }
                }
            }

            m_lock.unlock();
        }
    }
    
    public void save() {
        m_lock.lock();

        SaveData data = new SaveData();
        data.Disabled = m_disabled;
        data.Agents = m_agents;
        data.Pools = m_pools;
        
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(getSavePath()) , "UTF-8");
            Gson gson = new GsonBuilder().create();
            gson.toJson(data, writer);
            writer.close();
        }
        catch(Exception ex) {
            Loggers.SERVER.error("Failed to save personal builds toggle data: " + ex.toString());
        }
        finally {
            m_lock.unlock();
        }
    }

    private String getSavePath() {
        return Paths.get(m_serverPaths.getConfigDir(), "personal_builds_toggle.json").toString();
    }
    
	public boolean isDisabled() {
        return m_disabled;
    }
    
    private class SaveData {
        
        public Boolean Disabled;
        public Map<Integer, ToggleSetting> Agents;
        public Map<Integer, ToggleSetting> Pools;
     }
}