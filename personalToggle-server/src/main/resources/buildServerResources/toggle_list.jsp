<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<%@ page import="novemberdobby.teamcity.personalToggle.Constants" %>
<%@ page import="novemberdobby.teamcity.personalToggle.ToggleSetting"%>

<c:set var="toggle_url" value="<%=Constants.TOGGLE_URL%>"/>

<style type="text/css">
#agentsTable th, #agentsTable td {
  border: 1px solid #eaeaea;
}
</style>

<table id="agentsTable" cellpadding="4">
  <thead>
    <tr>
      <th>Agent</th>
      <th>Personal builds setting</th>
    </tr>
  </thead>
  <c:forEach items="${agents.entrySet()}" var="agent">
    <tr>
      <td><bs:agent agent="${agent.getKey()}" doNotShowOutdated="true" /></td>

      <td>
        <select id='setting_${agent.getKey().getId()}' onchange='BS.PersonalToggle.onChange(this, "${agent.getKey().getId()}")'>

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
    onChange: function(dropdown, id) {
      BS.ajaxRequest(window['base_uri'] + '${toggle_url}', {
        method: "POST",
        parameters: { 'id': id, 'setting': dropdown.value }
      });

      location.reload();
    }
  };
</script>