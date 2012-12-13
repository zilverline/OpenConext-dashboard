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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import nl.surfnet.coin.api.client.OpenConextOAuthClient;
import nl.surfnet.coin.api.client.domain.Group20;
import nl.surfnet.coin.api.client.domain.Person;
import nl.surfnet.coin.selfservice.dao.ConsentDao;
import nl.surfnet.coin.selfservice.domain.CoinUser;
import nl.surfnet.coin.selfservice.domain.CompoundServiceProvider;
import nl.surfnet.coin.selfservice.domain.GroupContext;
import nl.surfnet.coin.selfservice.domain.GroupContext.Group20Wrap;
import nl.surfnet.coin.selfservice.domain.IdentityProvider;
import nl.surfnet.coin.selfservice.domain.OAuthTokenInfo;
import nl.surfnet.coin.selfservice.domain.PersonAttributeLabel;
import nl.surfnet.coin.selfservice.domain.ServiceProvider;
import nl.surfnet.coin.selfservice.service.OAuthTokenService;
import nl.surfnet.coin.selfservice.service.ServiceProviderService;
import nl.surfnet.coin.selfservice.service.impl.CompoundSPService;
import nl.surfnet.coin.selfservice.service.impl.PersonAttributeLabelServiceJsonImpl;
import nl.surfnet.coin.selfservice.util.SpringSecurity;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Controller for the detail view(s) of a service (provider)
 */
@Controller
@RequestMapping
public class ServiceDetailController extends BaseController {

  @Resource(name = "providerService")
  private ServiceProviderService providerService;

  @Resource
  private CompoundSPService compoundSPService;

  @Resource(name = "oAuthTokenService")
  private OAuthTokenService oAuthTokenService;

  @Autowired
  private OpenConextOAuthClient apiClient;

  @Autowired
  private ConsentDao consentDao;

  @Resource(name = "personAttributeLabelService")
  private PersonAttributeLabelServiceJsonImpl personAttributeLabelService;

  @Value("${lmngDeepLinkBaseUrl}")
  private String lmngDeepLinkBaseUrl;

  @Value("${maxRecommendationEmails}")
  private int maxRecommendationEmails = 20;

  /**
   * Controller for detail page.
   * 
   * @param compoundSpId
   *          the compound service provider id
   * @return ModelAndView
   */
  @RequestMapping(value = "/app-detail")
  public ModelAndView serviceDetail(@RequestParam(value = "compoundSpId") long compoundSpId,
      @RequestParam(required = false) String revoked,
      @RequestParam(value = "refreshCache", required = false, defaultValue = "false") String refreshCache,
      @ModelAttribute(value = "selectedidp") IdentityProvider selectedidp) {
    Map<String, Object> m = new HashMap<String, Object>();
    CompoundServiceProvider compoundServiceProvider = compoundSPService
        .getCSPById(selectedidp, compoundSpId, Boolean.valueOf(refreshCache));
    m.put(COMPOUND_SP, compoundServiceProvider);

    String spEntityId = compoundServiceProvider.getServiceProviderEntityId();
    final Boolean mayHaveGivenConsent = consentDao.mayHaveGivenConsent(SpringSecurity.getCurrentUser().getUid(), spEntityId);
    m.put("mayHaveGivenConsent", mayHaveGivenConsent);

    final Map<String, PersonAttributeLabel> attributeLabelMap = personAttributeLabelService.getAttributeLabelMap();
    m.put("personAttributeLabels", attributeLabelMap);

    final List<OAuthTokenInfo> oAuthTokens = oAuthTokenService.getOAuthTokenInfoList(SpringSecurity.getCurrentUser().getUid(),
        compoundServiceProvider.getServiceProvider());

    m.put("oAuthTokens", oAuthTokens);

    m.put("revoked", revoked);

    m.put("lmngDeepLinkUrl", lmngDeepLinkBaseUrl);

    return new ModelAndView("app-detail", m);
  }

  @RequestMapping(value = "/app-recommend")
  public ModelAndView recommendApp(@RequestParam(value = "compoundSpId") long compoundSpId,
      @ModelAttribute(value = "selectedidp") IdentityProvider selectedidp) {
    Map<String, Object> m = new HashMap<String, Object>();

    CompoundServiceProvider compoundServiceProvider = compoundSPService.getCSPById(selectedidp, compoundSpId, false);
    m.put(COMPOUND_SP, compoundServiceProvider);
    m.put("maxRecommendationEmails", maxRecommendationEmails);
    return new ModelAndView("app-recommend", m);
  }

  @RequestMapping(value = "/do-app-recommend", method = RequestMethod.POST)
  public @ResponseBody
  String doRecommendApp(@RequestParam(value = "compoundSpId") long compoundSpId,
      @RequestParam(value = "recommendPersonalNote", required = false) String recommendPersonalNote,
      @RequestParam(value = "emailSelect2") String emailSelect2,
      @RequestParam(value = "detailAppStoreLink") String detailAppStoreLink,
      @CookieValue(value = "org.springframework.web.servlet.i18n.CookieLocaleResolver.LOCALE", required = false) String localeAbbr) {
    recommendPersonalNote = StringUtils.hasText(recommendPersonalNote) ? ((recommendPersonalNote.replace("\n\r", "").trim().length() == 0) ? null
        : recommendPersonalNote)
        : null;
    if (!StringUtils.hasText(emailSelect2)) {
      throw new RuntimeException("Required field emails addresses");
    }
    //TODO validate emails
    String[] recipients = emailSelect2.split(",");
    System.out.println(recipients);
    Locale locale = StringUtils.hasText(localeAbbr) ? new Locale(localeAbbr) : new Locale("en"); 
    System.out.println("locale :" + locale);
    System.out.println("compoundSp:" + compoundSpId);
    System.out.println("recommendPersonalNote:" + recommendPersonalNote);
    System.out.println("emailSelect2:" + emailSelect2);
    
    //TODO
    System.out.println("detailAppStoreLink: "+detailAppStoreLink);
    System.out.println( "app-detail.shtml?compoundSpId=X");
    CoinUser coinUser = SpringSecurity.getCurrentUser();
    System.out.println(coinUser.getDisplayName());
    // emailService,sendEmail(emailService.EMAIL_RECOMMENSDATION,map, recipients
    // )
    return "ok";
  }

  @RequestMapping("/groupsWithMembers.json")
  public @ResponseBody
  List<Group20Wrap> groupsWithMembers() {
    CoinUser coinUser = SpringSecurity.getCurrentUser();
    List<Group20> groups = apiClient.getGroups20(coinUser.getUid(), coinUser.getUid());
    GroupContext groupsWithMembers = new GroupContext();
    for (Group20 group : groups) {
      List<Person> members = apiClient.getGroupMembers(group.getId(), coinUser.getUid());
      groupsWithMembers.addGroup(group, members);
    }
    return groupsWithMembers.getEntries();
    //return groupsWithMembers.getEntries();
  //  return IOUtils.toString(new ClassPathResource("recommendations/emails.json").getInputStream());
  }

  @RequestMapping(value = "revokekeys.shtml")
  public RedirectView revokeKeys(@RequestParam(value = "compoundSpId") long compoundSpId,
      @RequestParam(value = "spEntityId") String spEntityId, @ModelAttribute(value = "selectedidp") IdentityProvider selectedidp) {
    final ServiceProvider sp = providerService.getServiceProvider(spEntityId, selectedidp.getId());
    oAuthTokenService.revokeOAuthTokens(SpringSecurity.getCurrentUser().getUid(), sp);
    return new RedirectView("app-detail.shtml?compoundSpId=" + compoundSpId + "&revoked=true");
  }
}
