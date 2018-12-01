<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>

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
  <c:forEach items="${agents}" var="agent">
    <tr>
      <td><bs:agent agent="${agent}" doNotShowOutdated="true" /></td>
      <td>sure</td>
    </tr>
  </c:forEach>

</table>