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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import nl.surfnet.coin.csa.Csa;
import nl.surfnet.coin.csa.model.InstitutionIdentityProvider;
import nl.surfnet.coin.csa.model.Service;
import nl.surfnet.coin.selfservice.service.EdugainApp;
import nl.surfnet.coin.selfservice.service.EdugainService;
import nl.surfnet.coin.selfservice.service.impl.PersonAttributeLabelServiceJsonImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Optional;

/**
 * Test for {@link nl.surfnet.coin.selfservice.control.ServiceDetailController}
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceDetailControllerTest {

  @InjectMocks
  private ServiceDetailController controller = new ServiceDetailController();

  @Mock
  private LocaleResolver localeResolver;

  @Mock
  private PersonAttributeLabelServiceJsonImpl labelService;

  @Mock
  private EdugainService edugainService;

  private HttpServletRequest request;

  @Mock
  private Csa csa;

  @Before
  public void setUp() throws Exception {
    request = new MockHttpServletRequest();
    request.getSession().setAttribute(BaseController.SELECTED_IDP, new InstitutionIdentityProvider("id", "name", "inst"));
  }

  @Test
  public void testSpDetail() throws Exception {
    Service service = getService();
    when(csa.getServiceForIdp("id", 1L)).thenReturn(service);
    final ModelAndView modelAndView = controller.serviceDetail(1L, null, false, request);
    assertEquals("app-detail", modelAndView.getViewName());
    assertEquals(service, modelAndView.getModelMap().get("service"));
  }

  @Test
  public void testEdugain() throws Exception {
    final EdugainApp edugainApp = new EdugainApp();
    when(edugainService.getApp(1L)).thenReturn(Optional.of(edugainApp));
    final ModelAndView modelAndView = controller.serviceDetail(1L, null, true, request);
    assertEquals("app-detail", modelAndView.getViewName());
    assertEquals(edugainApp, modelAndView.getModelMap().get("service"));
  }

  private Service getService() {
    return new Service(1L, "", "", "", false, null, "");
  }


}
