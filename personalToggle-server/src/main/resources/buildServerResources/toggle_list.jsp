<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<%@ page import="novemberdobby.teamcity.personalToggle.Constants" %>
<%@ page import="novemberdobby.teamcity.personalToggle.ToggleSetting"%>

<c:set var="toggle_url" value="<%=Constants.TOGGLE_URL%>"/>

<style type="text/css">
#agentsTable th, #agentsTable td {
  border: 1px solid #eaeaea;
}
#poolsTable th, #poolsTable td {
  border: 1px solid #eaeaea;
}
</style>

<br>
<div>From currently connected and enabled agents:</div>
<li style="<c:if test='${canRunPersonal == 0}'>color:red</c:if>">${canRunPersonal} can run personal builds</li>
<li style="<c:if test='${canRunNonPersonal == 0}'>color:red</c:if>">${canRunNonPersonal} can run non-personal builds</li>
<br>
<div><i>Agents without a setting below will fall back to their pool's behaviour. Settings take effect immediately.</i></div>
<br>

<table id="poolsTable" cellpadding="4">
  <thead>
    <tr>
      <th>Pool</th>
      <th>Personal builds setting</th>
    </tr>
  </thead>
  <c:forEach items="${pools.entrySet()}" var="pool">
    <tr>
      <td><a href='/agents.html?tab=agentPools#${pool.getKey().getAgentPoolId()}'>${pool.getKey().getName()}</a></td>

      <td>
        <select id='setting_pool_${pool.getKey().getAgentPoolId()}' onchange='BS.PersonalToggle.onChange(this, "${pool.getKey().getAgentPoolId()}", true)'>

          <c:set var="settingValues" value="<%=ToggleSetting.values()%>"/>
          <c:forEach items="${settingValues}" var="val">
            <c:set var="selected" value=""/>
            <c:if test='${pool.getValue().toString() == val.toString()}'>
              <c:set var='selected' value="selected='selected'"/>
            </c:if>

            <option ${selected} value="${val.name()}">${val}</option>
          </c:forEach>
        </select>
      </td>
    </tr>
  </c:forEach>
</table>

<br>

<table id="agentsTable" cellpadding="4">
  <thead>
    <tr>
      <th>Agent</th>
      <th>Personal builds setting</th>
    </tr>
  </thead>
  <c:forEach items="${agents.entrySet()}" var="agent">
    <tr>
      <td><bs:agentDetailsFullLink agent="${agent.getKey()}" doNotShowOutdated="true" /></td>

      <td>
        <select id='setting_agent_${agent.getKey().getId()}' onchange='BS.PersonalToggle.onChange(this, "${agent.getKey().getId()}", false)'>

          <c:set var="settingValues" value="<%=ToggleSetting.values()%>"/>
          <c:forEach items="${settingValues}" var="val">
            <c:set var="selected" value=""/>
            <c:if test='${agent.getValue().toString() == val.toString()}'>
              <c:set var='selected' value="selected='selected'"/>
            </c:if>

            <option ${selected} value="${val.name()}">${val}</option>
          </c:forEach>
        </select>
      </td>
    </tr>
  </c:forEach>
</table>

<script type="text/javascript">
  BS.PersonalToggle = {
    onChange: function(dropdown, id, isPool) {
      BS.ajaxRequest(window['base_uri'] + '${toggle_url}', {
        method: "POST",
        parameters: { 'isPool': isPool, 'id': id, 'setting': dropdown.value }
      });

      location.reload();
    }
  };
</script>