<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://rhn.redhat.com/rhn" prefix="rhn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<!-- header.jsp -->

<c:set var="custom_header" scope="page" value="${rhn:getConfig('java.custom_header')}" />

<div id="messages-container"></div>

<div class="header-content container-fluid">
  <div class="navbar-header d-flex flex-row">
    <a class="navbar-toggle" data-bs-toggle="collapse" href="#spacewalk-aside">
      <i class="fa fa-bars" aria-hidden="true"></i>
    </a>
    <div id="breadcrumb">
      <c:if test="${! empty custom_header}">
        <div class="custom-text">
          <c:out value="${custom_header}" escapeXml="false"/>
        </div>
      </c:if>
    </div>
  </div>

  <rhn:require acl="user_authenticated()">
    <ul class="nav navbar-nav navbar-controls">
      <li id="notifications">
        <script>
          spaImportReactPage('notifications/notifications');
        </script>
      </li>
      <c:if test="${requestScope.legends != null}">
        <li class="legend" id="legend-box-wrapper">
          <a href="#" class="toggle-box" data-bs-toggle="collapse" data-bs-target="#legend-box">
            <i class="fa fa-eye" aria-hidden="true"></i>
          </a>
          <div id="legend-box" class="box-wrapper collapse">
            <jsp:include page="/WEB-INF/includes/legends.jsp" />
          </div>
        </li>
      </c:if>
      <li class="search" id="header-search">
        <script>
          spaImportReactPage('header/search');
        </script>
      </li>
      <li id="ssm-box" class="ssm-box hide-overflow">
        <div id="ssm-counter"></div>
        <script type="text/javascript">
          window.csrfToken = '<c:out value="${csrf_token}" />';
          spaImportReactPage('systems/ssm/ssm-counter')
            .then(function(module) {
              module.renderer("ssm-counter", {})
            });
        </script>
      </li>
      <li>
        <a href="/rhn/account/UserDetails.do"
          title="${requestScope.session.user.login}">
            <rhn:icon type="header-user" /><span class="menu-link">${requestScope.session.user.login}</span>
        </a>
      </li>
      <li>
        <rhn:require acl="user_role(org_admin)">
          <a href="/rhn/multiorg/OrgConfigDetails.do" title="${requestScope.session.user.org.name}">
            <rhn:icon type="header-sitemap" /><span class="menu-link">${requestScope.session.user.org.name}</span>
          </a>
        </rhn:require>
        <rhn:require acl="not user_role(org_admin)">
          <span class="spacewalk-header-non-link" title="${requestScope.session.user.org.name}">
            <rhn:icon type="header-sitemap" /><span class="menu-link">${requestScope.session.user.org.name}</span>
          </span>
        </rhn:require>
      </li>
      <li>
        <a href="/rhn/account/UserPreferences.do" role="button" title="<bean:message key="header.jsp.preferences" />"
            alt="<bean:message key="header.jsp.preferences" />">
          <rhn:icon type="header-preferences"/>
        </a>
      </li>
      <li>
      <c:choose>
        <c:when test="${rhn:getConfig('java.sso')}">
          <a data-senna-off href="/rhn/manager/sso/logout" role="button" title="<bean:message key="header.jsp.signout" />"
            alt="<bean:message key="header.jsp.signout" />">
        </c:when>
        <c:otherwise>
          <a data-senna-off href="/rhn/Logout.do" role="button" title="<bean:message key="header.jsp.signout" />"
            alt="<bean:message key="header.jsp.signout" />">
        </c:otherwise>
      </c:choose>
        <rhn:icon type="header-signout" />
        </a>
      </li>
    </ul>
  </rhn:require>
</div>
<!-- end header.jsp -->
