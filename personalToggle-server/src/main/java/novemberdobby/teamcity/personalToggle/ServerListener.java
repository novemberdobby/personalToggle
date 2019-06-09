package novemberdobby.teamcity.personalToggle;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.util.EventDispatcher;

public class ServerListener extends BuildServerAdapter {

    ToggleController m_controller;

    public ServerListener(EventDispatcher<BuildServerListener> eventDispatcher, ToggleController controller) {
        m_controller = controller;
        eventDispatcher.addListener(this);
    }

    @Override
    public void serverStartup() {
        m_controller.load();
    }
}