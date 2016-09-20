/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2015 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server.events;

import com.docdoku.core.document.DocumentRevision;
import com.docdoku.core.meta.Tag;

/**
 * @author Florent Garin
 */
public class TagEvent {

    private Tag observedTag;

    private DocumentRevision taggableDocument;



    public TagEvent(Tag observedTag, DocumentRevision taggableDocument) {
        this.observedTag = observedTag;
        this.taggableDocument = taggableDocument;
    }

    public TagEvent(Tag observedTag) {
        this.observedTag = observedTag;
    }

    public DocumentRevision getTaggableDocument() {
        return taggableDocument;
    }

    public void setTaggableDocument(DocumentRevision taggableDocument) {
        this.taggableDocument = taggableDocument;
    }

    public Tag getObservedTag() {
        return observedTag;
    }

    public void setObservedTag(Tag observedTag) {
        this.observedTag = observedTag;
    }
}