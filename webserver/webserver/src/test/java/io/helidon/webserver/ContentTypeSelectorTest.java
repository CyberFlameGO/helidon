/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.webserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.helidon.common.http.MediaType;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ContentTypeSelector}.
 */
public class ContentTypeSelectorTest {

    @Test
    public void testContentTypeSelection() throws Exception {
        Map<String, MediaType> map = new HashMap<>();
        map.put("txt", MediaType.create("foo", "bar"));
        ContentTypeSelector selector = new ContentTypeSelector(map);
        // Empty headers
        RequestHeaders headers = mock(RequestHeaders.class);
        when(headers.isAccepted(any())).thenReturn(true);
        when(headers.acceptedTypes()).thenReturn(Collections.emptyList());
        assertThat(selector.determine("foo.xml", headers), is(MediaType.APPLICATION_XML));
        assertThat(selector.determine("foo.txt", headers), is(MediaType.create("foo", "bar")));
        assertThat(selector.determine("foo.undefined", headers), is(MediaType.APPLICATION_OCTET_STREAM));
        assertThat(selector.determine("undefined", headers), is(MediaType.APPLICATION_OCTET_STREAM));
        // Accept text/html
        headers = mock(RequestHeaders.class);
        when(headers.acceptedTypes()).thenReturn(Collections.singletonList(MediaType.TEXT_HTML));
        assertThat(selector.determine("foo.undefined", headers), is(MediaType.TEXT_HTML));
    }

    @Test
    public void testInvalidFile(){
        ContentTypeSelector selector = new ContentTypeSelector(Collections.emptyMap());
        RequestHeaders headers = mock(RequestHeaders.class);
        when(headers.isAccepted(any())).thenReturn(false);
        when(headers.acceptedTypes()).thenReturn(Collections.singletonList(MediaType.TEXT_HTML));
        HttpException ex = assertThrows(HttpException.class, () -> { selector.determine("foo.xml", headers); });
        assertThat(ex.getMessage(), is("Not accepted media-type!"));
    }
}
