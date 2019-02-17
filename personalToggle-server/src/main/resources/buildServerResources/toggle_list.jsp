<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

<%@ page import="novemberdobby.teamcity.personalToggle.Constants" %>

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
      <th>Personal builds enabled</th>
    </tr>
  </thead>
  <c:forEach items="${agents.entrySet()}" var="agent">
    <tr>
      <td><bs:agent agent="${agent.getKey()}" doNotShowOutdated="true" /></td>
      <c:set var='chkd' value='' />
      <c:if test='${agent.getValue()}'>
        <c:set var='chkd' value='checked'/>
      </c:if>

      <td><input type='checkbox' id='enabled_${agent.getKey().getId()}' onchange='BS.PersonalToggle.onToggle(this, "${agent.getKey().getId()}")' ${chkd} /></td>
    </tr>
  </c:forEach>
</table>

<script type="text/javascript">
  BS.PersonalToggle = {
    onToggle: function(checkbox, id) {
      BS.ajaxRequest(window['base_uri'] + '${toggle_url}', {
        method: "POST",
        parameters: { 'id': id, 'enabled': checkbox.checked }
      });

      location.reload();
    }
  };
</script>