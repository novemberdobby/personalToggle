package novemberdobby.teamcity.personalToggle;

import java.util.Map;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.BuildAgent;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.buildDistribution.BuildDistributorInput;
import jetbrains.buildServer.serverSide.buildDistribution.QueuedBuildInfo;
import jetbrains.buildServer.serverSide.buildDistribution.StartBuildPrecondition;
import jetbrains.buildServer.serverSide.buildDistribution.WaitReason;

public class AgentLimiter implements StartBuildPrecondition {

    ToggleStatus m_status;
    SBuildServer m_server;

    public AgentLimiter(@NotNull final ToggleStatus status, @NotNull final SBuildServer server) {
        m_status = status;
        m_server = server;
    }

    @Override
    public WaitReason canStart(QueuedBuildInfo queuedBuild, Map<QueuedBuildInfo, BuildAgent> canBeStarted,
            BuildDistributorInput buildDistributorInput, boolean emulationMode) {

        long buildId = queuedBuild.getBuildPromotionInfo().getId();
        Loggers.SERVER.info("ID: " + buildId);

        SBuild build = m_server.findBuildInstanceById(buildId);
        if(build == null || !build.isPersonal()) {
            
        }

        return null;
    }
}