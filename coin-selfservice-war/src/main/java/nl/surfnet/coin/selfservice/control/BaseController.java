/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.selfservice.control;

import nl.surfnet.coin.csa.model.InstitutionIdentityProvider;
import nl.surfnet.coin.selfservice.domain.NotificationMessage;
import nl.surfnet.coin.selfservice.service.NotificationService;
import nl.surfnet.coin.selfservice.util.AjaxResponseException;
import nl.surfnet.coin.selfservice.util.SpringSecurity;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.LocaleResolver;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Locale;

/**
 * Abstract controller used to set model attributes to the request
 */
@Controller
public abstract class BaseController implements ApplicationContextAware {

  /**
   * The name of the key under which all services are
   * stored
   */
  public static final String SERVICES = "services";

  /**
   * The name of the key under which a service is stored
   * for the detail view
   */
  public static final String SERVICE = "service";

  /**
   * The name of the key under which all the identity providers are stored
   * for the identity switch view
   */
  public static final String INSTITUTION_IDENTITY_PROVIDERS = "institutionIdentityProviders";

  /**
   * The name of the key under which the switched identity is stored
   */
  public static final String SWITCHED_IDENTITY_SWITCH = "switchedIdentitySwitch";

  /**
   * The name of the key under which we store the info if a logged user is
   * allowed to request connections / disconnects
   */
  public static final String SERVICE_APPLY_ALLOWED = "applyAllowed";

  /**
   * The name of the key under which we store the info if a logged user is
   * allowed to ask questions
   */
  public static final String SERVICE_QUESTION_ALLOWED = "questionAllowed";

  /**
   * The name of the key under which we store the info if the status of a
   * technical connection is visible to the current user.
   */
  public static final String SERVICE_CONNECTION_VISIBLE = "connectionVisible";

  /**
   * The name of the key under which we store the info if the connection facet is visible to the current user.
   */
  public static final String FACET_CONNECTION_VISIBLE = "facetConnectionVisible";

  /**
   * The name of the key that defines whether a deeplink to SURFMarket should be
   * shown.
   */
  public static final String DEEPLINK_TO_SURFMARKET_ALLOWED = "deepLinkToSurfMarketAllowed";

  /**
   * The name of the key under which we store the token used to prevent session
   * hijacking
   */
  public static final String TOKEN_CHECK = "tokencheck";

  /**
   * The name of the key under which we store the notifications
   */
  public static final String NOTIFICATIONS = "notificationMessage";

  /**
   * The name of the key under which we store the info if the notifications for
   * licenses/linked services were generated already
   */
  public static final String NOTIFICATION_POPUP_CLOSED = "notificationPopupClosed";

  /**
   * The name of the key under which we store the information from Api regarding
   * group memberships and actual members for auto-completion in the
   * recommendation modal popup.
   */
  public static final String GROUPS_WITH_MEMBERS = "groupsWithMembers";

  public static final String SHOW_ARP_MATCHES_PROVIDED_ATTRS = "showArpMatchesProvidedAttrs";
  /**
   * Key for the selectedIdp in the session
   */
  public static final String SELECTED_IDP = "selectedIdp";

  @Resource
  private NotificationService notificationService;

  @Resource(name = "localeResolver")
  protected LocaleResolver localeResolver;

  protected ApplicationContext context;

  @ModelAttribute(value = "idps")
  public List<InstitutionIdentityProvider> getMyInstitutionIdps() {
    return SpringSecurity.getCurrentUser().getInstitutionIdps();
  }

  @ModelAttribute(value = "locale")
  public Locale getLocale(HttpServletRequest request) {
    return localeResolver.resolveLocale(request);
  }

  protected InstitutionIdentityProvider getSelectedIdp(HttpServletRequest request) {
    final InstitutionIdentityProvider selectedIdp = (InstitutionIdentityProvider) request.getSession().getAttribute(SELECTED_IDP);
    if (selectedIdp != null) {
      return selectedIdp;
    }
    return selectProvider(request, SpringSecurity.getCurrentUser().getIdp().getId());
  }

  protected InstitutionIdentityProvider switchIdp(HttpServletRequest request, String switchIdpId) {
    Assert.hasText(switchIdpId);
    return selectProvider(request, switchIdpId);
  }

  private InstitutionIdentityProvider selectProvider(HttpServletRequest request, String idpId) {
    Assert.hasText(idpId);
    for (InstitutionIdentityProvider idp : SpringSecurity.getCurrentUser().getInstitutionIdps()) {
      if (idp.getId().equals(idpId)) {
        request.getSession().setAttribute(SELECTED_IDP, idp);
        SpringSecurity.getCurrentUser().setIdp(idp);
        return idp;
      }
    }
    throw new RuntimeException(idpId + " is unknown for " + SpringSecurity.getCurrentUser().getUsername());
  }

  /**
   * Get notifications from the session (if available) and place as model
   * attribute. Create/generate possible notifications if not found on session
   * and add to session.
   */
  @ModelAttribute(value = "notifications")
  public NotificationMessage getNotifications(HttpServletRequest request) {
    NotificationMessage notifications = (NotificationMessage) request.getSession().getAttribute(NOTIFICATIONS);
    if (notifications == null) {
      InstitutionIdentityProvider idp = getSelectedIdp(request);
      notifications = notificationService.getNotifications(idp);
      request.getSession().setAttribute(NOTIFICATIONS, notifications);
    }
    return notifications;
  }

  protected void notificationPopupClosed(HttpServletRequest request) {
    request.getSession().setAttribute(NOTIFICATION_POPUP_CLOSED, Boolean.TRUE);
  }

  /**
   * Handler for {@link AjaxResponseException}. We don't want a 500, but a 400
   * and we want to stream the error message direct to the javaScript
   *
   * @param e the exception
   * @return the response body
   */
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  @ResponseBody
  @ExceptionHandler(AjaxResponseException.class)
  public Object handleAjaxResponseException(AjaxResponseException e) {
    return e.getMessage();
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.context = applicationContext;
  }

}