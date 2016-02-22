/*
 * Copyright (c) 2015 Franjo Žilić <frenky666@gmail.com>
 *
 * Permission to use, copy, modify, and distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

package com.github.usedrarely.spring.rate.limit.options.annotation;

import com.github.usedrarely.spring.rate.limit.RateLimited;
import com.github.usedrarely.spring.rate.limit.RateLimitedRetry;
import com.github.usedrarely.spring.rate.limit.options.Options;
import com.github.usedrarely.spring.rate.limit.options.OptionsResolver;
import com.github.usedrarely.spring.rate.limit.options.exception.IllegalConfigurationException;
import com.github.usedrarely.spring.rate.limit.options.exception.OptionsException;
import org.aspectj.lang.JoinPoint;

import static com.github.usedrarely.spring.rate.limit.util.JoinPointUtil.findAnnotation;

public class AnnotationOptionsResolver implements OptionsResolver {

  @Override
  public Options resolve(final String key, final RateLimited rateLimited, final JoinPoint joinPoint) throws OptionsException {
    if (!RateLimited.Configuration.ANNOTATION.equals(rateLimited.configuration())) {
      throw new IllegalConfigurationException("Unsupported configuration");
    }

    if (!rateLimited.enabled()) {
      return AnnotationOptions.disabled(key);
    }

    if (rateLimited.maxRequests() == -1 || rateLimited.interval().interval() == -1) {
      throw new IllegalConfigurationException("Annotation configuration is enabled, maxRequests and interval must be greater then 0");
    }

    final AnnotationOptions options = AnnotationOptions.enabled(key, rateLimited.maxRequests(), AnnotationOptions.intervalOf(rateLimited.interval().interval(), rateLimited.interval().unit()));

    // attempt to locate retry configuration
    final RateLimitedRetry retry = findAnnotation(joinPoint, RateLimitedRetry.class);
    if (retry != null) {
      options.enableRetry(retry.retryCount(), AnnotationOptions.intervalOf(retry.interval().interval(), retry.interval().unit()));
    }

    return options;
  }

  @Override
  public boolean supports(final String key, final RateLimited rateLimited) {
    return RateLimited.Configuration.ANNOTATION.equals(rateLimited.configuration());
  }
}