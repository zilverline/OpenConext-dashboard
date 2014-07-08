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
package nl.surfnet.coin.selfservice.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;

import nl.surfnet.coin.shared.domain.DomainObject;

import org.hibernate.annotations.Proxy;

/**
 * ImageHolder.java
 *
 */
@SuppressWarnings("serial")
@Entity
@Proxy(lazy = false)
public class ImageHolder extends DomainObject {

  @Column(name = "filename")
  private String filename;
  
  @Column(name = "image")
  @Lob
  private byte[] image;
  
  
}
